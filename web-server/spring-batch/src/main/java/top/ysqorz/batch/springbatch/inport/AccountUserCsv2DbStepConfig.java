package top.ysqorz.batch.springbatch.inport;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import top.ysqorz.batch.springbatch.batch.RowItemDBWriter;
import top.ysqorz.batch.springbatch.batch.ValidateItemProcessor;
import top.ysqorz.batch.springbatch.mapper.ICollectorMapper;
import top.ysqorz.batch.springbatch.mapper.ICoreUserMapper;
import top.ysqorz.batch.springbatch.mapper.ILoginAccountMapper;
import top.ysqorz.batch.springbatch.mapper.ITaskListMapper;
import top.ysqorz.batch.springbatch.model.Constant;
import top.ysqorz.batch.springbatch.model.po.Collector;
import top.ysqorz.batch.springbatch.model.po.CoreUser;
import top.ysqorz.batch.springbatch.model.po.LoginAccount;
import top.ysqorz.batch.springbatch.model.po.TaskList;
import top.ysqorz.batch.springbatch.model.vo.AccountUserVO;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/18
 */
//@ConditionalOnProperty(name = "custom.csv.account-user", havingValue = "csv")
@Configuration
@Slf4j
public class AccountUserCsv2DbStepConfig {
    @Resource
    private StepBuilderFactory stepBuilderFactory;
    @Value("${custom.login.default-password:Zwsoft}")
    private String defaultPassword;
    @Value("${custom.csv.account-user}")
    private String csvFileName;
    private ClassPathResource sourceCsv;

    @Bean
    public FlatFileItemReader<AccountUserVO> accountUserCsvReader() {
        sourceCsv = new ClassPathResource(csvFileName);
        return new FlatFileItemReaderBuilder<AccountUserVO>()
                .name("accountUserCsvReader")
                .saveState(false)
                .linesToSkip(1) // 跳过第一行(表头)
                .resource(sourceCsv)
                .delimited()
                .names("account", "userName", "contact", "email", "status") // csv的列的顺序不能改变
                .targetType(AccountUserVO.class)
                .build();
    }

    @Bean
    public ValidateItemProcessor<AccountUserVO> accountUserValidateProcessor() {
        return new ValidateItemProcessor<>();
    }

    @Bean
    public ItemWriter<AccountUserVO> accountUserDbWriter(@Qualifier("targetSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws IOException {
        return new RowItemDBWriter<>(sqlSessionFactory, this::createAccountAndUser, sourceCsv.getFile(), AccountUserVO.class);
    }

    @Bean
    public Step accountUserCsv2DbStep(FlatFileItemReader<AccountUserVO> reader,
                                      ValidateItemProcessor<AccountUserVO> processor,
                                      ItemWriter<AccountUserVO> writer) {
        return stepBuilderFactory.get("accountUserCsv2DbStep")
                .<AccountUserVO, AccountUserVO>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    private void createAccountAndUser(AccountUserVO accountUserVO, SqlSession sqlSession) {
        String account = accountUserVO.getAccount();
        String userName = accountUserVO.getUserName();
        ILoginAccountMapper accountMapper = sqlSession.getMapper(ILoginAccountMapper.class);
        ICoreUserMapper userMapper = sqlSession.getMapper(ICoreUserMapper.class);
        ITaskListMapper taskListMapper = sqlSession.getMapper(ITaskListMapper.class);
        ICollectorMapper collectorMapper = sqlSession.getMapper(ICollectorMapper.class);
        // 是否已经存在同名的账号
        LoginAccount loginAccount = accountMapper.selectByAccount(account);
        if (ObjectUtil.isNotEmpty(loginAccount)) {
            throw new RuntimeException("账号已存在，Account：" + account);
        }
        // 是否已经存在同名的用户
        CoreUser coreUser = userMapper.selectByName(userName);
        if (ObjectUtil.isNotEmpty(coreUser)) {
            throw new RuntimeException("用户已存在，UserName：" + userName);
        }

        // 是否已经存在同名的任务箱
        TaskList taskList = taskListMapper.selectByUserID(userName);
        if (ObjectUtil.isNotEmpty(taskList)) {
            throw new RuntimeException("个人任务箱已存在，UserID：" + userName);
        }
        // 是否已存在根收藏夹
        Collector collector = collectorMapper.selectRootByOwner(userName);
        if (ObjectUtil.isNotEmpty(collector)) {
            throw new RuntimeException("个人根收藏夹已存在，Owner：" + userName);
        }
        // 创建账号
        loginAccount = accountUserVO.toLoginAccount(defaultPassword);
        if (accountMapper.insert(loginAccount) != 1) {
            throw new RuntimeException("创建账号失败：" + JSONUtil.toJsonStr(loginAccount));
        }
        // 创建用户
        coreUser = accountUserVO.toCoreUser();
        if (userMapper.insert(coreUser) != 1) {
            throw new RuntimeException("创建用户失败：" + JSONUtil.toJsonStr(coreUser));
        }
        // 创建任务箱
        taskList = new TaskList()
                .setUserID(userName)
                .setIsConsigned(Constant.MINUS);
        if (taskListMapper.insert(taskList) != 1) {
            throw new RuntimeException("创建个人任务箱失败：" + JSONUtil.toJsonStr(taskList));
        }
        // 创建默认收藏夹
        collector = new Collector().setName(Constant.ROOT);
        collector.setOwner(userName);
        if (collectorMapper.insert(collector) != 1) {
            throw new RuntimeException("创建个人根收藏夹失败：" + JSONUtil.toJsonStr(collector));
        }
    }
}

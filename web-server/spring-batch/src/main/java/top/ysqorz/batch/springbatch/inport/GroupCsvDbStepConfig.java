package top.ysqorz.batch.springbatch.inport;

import cn.hutool.core.util.ObjectUtil;
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
import top.ysqorz.batch.springbatch.mapper.ICoreGroupMapper;
import top.ysqorz.batch.springbatch.mapper.ICoreUserMapper;
import top.ysqorz.batch.springbatch.mapper.ILoginAccountMapper;
import top.ysqorz.batch.springbatch.mapper.IRGroupMemberMapper;
import top.ysqorz.batch.springbatch.model.po.CoreGroup;
import top.ysqorz.batch.springbatch.model.po.CoreUser;
import top.ysqorz.batch.springbatch.model.po.LoginAccount;
import top.ysqorz.batch.springbatch.model.po.RGroupMember;
import top.ysqorz.batch.springbatch.model.vo.GroupMemberVO;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/21
 */
@Configuration
public class GroupCsvDbStepConfig {
    @Resource
    private StepBuilderFactory stepBuilderFactory;
    @Value("${custom.csv.group-member}")
    private String csvFileName;
    private ClassPathResource sourceCsv;

    @Bean
    public FlatFileItemReader<GroupMemberVO> groupMemberCsvReader() {
        sourceCsv = new ClassPathResource(csvFileName);
        return new FlatFileItemReaderBuilder<GroupMemberVO>()
                .name("groupMemberCsvReader")
                .saveState(false)
                .linesToSkip(1) // 跳过第一行(表头)
                .resource(sourceCsv)
                .delimited()
                .names("account", "groupName") // csv的列的顺序不能改变
                .targetType(GroupMemberVO.class)
                .build();
    }

    @Bean
    public ValidateItemProcessor<GroupMemberVO> groupMemberValidateProcessor() {
        return new ValidateItemProcessor<>();
    }

    @Bean
    public ItemWriter<GroupMemberVO> groupMemberDbWriter(@Qualifier("targetSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws IOException {
        return new RowItemDBWriter<>(sqlSessionFactory, this::createGroupMember, sourceCsv.getFile(), GroupMemberVO.class);
    }

    @Bean
    public Step groupMemberCsv2DbStep(FlatFileItemReader<GroupMemberVO> reader,
                                      ValidateItemProcessor<GroupMemberVO> processor,
                                      ItemWriter<GroupMemberVO> writer) {
        return stepBuilderFactory.get("groupMemberCsv2DbStep")
                .<GroupMemberVO, GroupMemberVO>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    private void createGroupMember(GroupMemberVO groupMemberVO, SqlSession sqlSession) {
        String account = groupMemberVO.getAccount();
        String groupName = groupMemberVO.getGroupName();
        if (ObjectUtil.isEmpty(account)) {
            throw new RuntimeException("账号不能为空");
        }
        if (ObjectUtil.isEmpty(groupName)) {
            throw new RuntimeException("用户组名不能为空");
        }
        ILoginAccountMapper accountMapper = sqlSession.getMapper(ILoginAccountMapper.class);
        ICoreUserMapper userMapper = sqlSession.getMapper(ICoreUserMapper.class);
        ICoreGroupMapper groupMapper = sqlSession.getMapper(ICoreGroupMapper.class);
        IRGroupMemberMapper memberMapper = sqlSession.getMapper(IRGroupMemberMapper.class);
        // 账号是否存在
        LoginAccount loginAccount = accountMapper.selectByAccount(account);
        if (ObjectUtil.isEmpty(loginAccount)) {
            throw new RuntimeException("账号不存在，Account：" + account);
        }
        // 用户是否存在
        String userName = loginAccount.getUserName();
        CoreUser user = userMapper.selectByName(userName);
        if (ObjectUtil.isEmpty(user)) {
            throw new RuntimeException("用户不存在，Name：" + userName);
        }
        // 用户组是否存在
        CoreGroup group = groupMapper.selectByName(groupName);
        if (ObjectUtil.isEmpty(group)) {
            throw new RuntimeException("用户组不存在，Name：" + groupName);
        }
        // 成员关系是否已存在
        RGroupMember member = memberMapper.selectByBothUUID(group, user);
        if (ObjectUtil.isNotEmpty(member)) {
            throw new RuntimeException(String.format("用户\"%s\"已经加入用户组\"%s\"", userName, groupName));
        }
        member = new RGroupMember(group, user);
        if (memberMapper.insert(member) != 1) {
            throw new RuntimeException(String.format("用户\"%s\"加入用户组\"%s\"失败", userName, groupName));
        }
    }

}

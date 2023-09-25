package top.ysqorz.batch.springbatch.export;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;
import top.ysqorz.batch.springbatch.batch.MyBatisPlusPagingReader;
import top.ysqorz.batch.springbatch.mapper.ICoreUserMapper;
import top.ysqorz.batch.springbatch.model.po.CoreUser;

import javax.annotation.Resource;
import java.util.Date;

//@EnableBatchProcessing
//@Configuration
@Slf4j
public class CoreUserDb2CsvJobConfig {
    @Resource
    private JobBuilderFactory jobBuilderFactory;
    @Resource
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public MyBatisPlusPagingReader<CoreUser> coreUserReader(@Qualifier("originSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return MyBatisPlusPagingReader.<CoreUser>builder()
                .saveState(false)
                .sqlSessionFactory(sqlSessionFactory)
                .mapperClass(ICoreUserMapper.class)
                .pageSize(10)
                .build();
    }

    @Bean
    public FlatFileItemWriter<CoreUser> coreUserCsvWriter() {
        return new FlatFileItemWriterBuilder<CoreUser>()
                .name("coreUserCsvWriter")
                .resource(new ClassPathResource("csv/batch_CoreUser.csv"))
                .lineAggregator(new DelimitedLineAggregator<>())
                .build();
    }

    @Bean
    public ItemWriter<CoreUser> coreUserPrinter(){
        return users -> log.info("写出之后：{}", JSONUtil.toJsonStr(users));
    }

    @Bean
    public ItemProcessor<CoreUser, CoreUser> coreUserProcessor() {
        return user -> {
            user.setUUID(user.getUUID().replace("-", "").toUpperCase()); // 去除UUID的-并转成大写
            user.setCreateTimeStamp(DateUtil.formatDateTime(new Date(Long.parseLong(user.getCreateTimeStamp())))); // 格式化创建时间戳
            if (!ObjectUtils.isEmpty(user.getModifyTimeStamp())) {
                user.setModifyTimeStamp(DateUtil.formatDateTime(new Date(Long.parseLong(user.getModifyTimeStamp())))); // 格式化修改时间戳
            }
            log.info("处理之后：{}", JSONUtil.toJsonStr(user));
            return user;
        };
    }

    @Bean
    public Step coreUserDb2CsvStep(ItemReader<CoreUser> coreUserReader, ItemProcessor<CoreUser, CoreUser> coreUserProcessor,
                                   FlatFileItemWriter<CoreUser> coreUserCsvWriter) {
        return stepBuilderFactory.get("coreUserDb2CsvStep")
                .<CoreUser, CoreUser>chunk(3)
                .reader(coreUserReader)
                .processor(coreUserProcessor)
                .writer(coreUserCsvWriter) // 调用带有@Bean的方法会返回代理对象？？会再创建一个吗
                .build();
    }

    @Bean
    public Job coreUserDb2CsvJob(Step coreUserDb2CsvStep) {
        return jobBuilderFactory.get("coreUserDb2CsvJob")
                .start(coreUserDb2CsvStep)
                .incrementer(new RunIdIncrementer()) //保证可以多次执行
                .build();
    }
}


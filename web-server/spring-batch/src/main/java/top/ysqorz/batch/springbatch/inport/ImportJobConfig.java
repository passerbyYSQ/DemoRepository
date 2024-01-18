package top.ysqorz.batch.springbatch.inport;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/21
 */
@Configuration
public class ImportJobConfig {
    @Resource
    private JobBuilderFactory jobBuilderFactory;
    @Resource
    private Step accountUserCsv2DbStep;
    @Resource
    private Step groupMemberCsv2DbStep;
    @Resource
    private Step documentCsv2DbStep;

    @Bean
    public Job importJob() {
        return jobBuilderFactory.get("importJob")
                .start(documentCsv2DbStep)
//                .next(groupMemberCsv2DbStep)
                .incrementer(new RunIdIncrementer()) // 保证可以多次执行
                .build();
    }
}

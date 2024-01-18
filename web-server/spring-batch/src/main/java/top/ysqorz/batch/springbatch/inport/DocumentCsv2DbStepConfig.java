package top.ysqorz.batch.springbatch.inport;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import top.ysqorz.batch.springbatch.model.vo.BODocumentVO;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/10/11
 */
@Configuration
public class DocumentCsv2DbStepConfig {
    @Resource
    private StepBuilderFactory stepBuilderFactory;
    @Resource
    private ThreadPoolTaskExecutor batchExecutor;


    @Bean
    public FlatFileItemReader<BODocumentVO> documentCsvReader() {
        List<String> fieldNames = Arrays.stream(ReflectUtil.getFieldsDirectly(BODocumentVO.class, false))
                .map(Field::getName).collect(Collectors.toList());
        return new FlatFileItemReaderBuilder<BODocumentVO>()
                .name("documentCsvReader")
                .saveState(false)
                .linesToSkip(1) // 跳过第一行(表头)
                .resource(new ClassPathResource("csv/bo_Document_ALL_ALL.csv"))
                .recordSeparatorPolicy(new DefaultRecordSeparatorPolicy("{["))
                .delimited()
                .quoteCharacter('"')
                .delimiter(",")
                .names(Convert.toStrArray(fieldNames))
                .targetType(BODocumentVO.class)
                .build();
    }

    @Bean
    public ItemWriter<BODocumentVO> documentDbWriter(@Qualifier("targetSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws IOException {
        return new ItemWriter<BODocumentVO>() {
            @Override
            public void write(List<? extends BODocumentVO> items) throws Exception {
                System.out.println(items);
            }
        };
    }


    @Bean
    public Step documentCsv2DbStep(FlatFileItemReader<BODocumentVO> reader,
                                   ItemWriter<BODocumentVO> writer) {
        return stepBuilderFactory.get("documentCsv2DbStep")
                .<BODocumentVO, BODocumentVO>chunk(10)
                .reader(reader)
                //.processor(writer.createValidateItemProcessor())
                .writer(writer)
                .taskExecutor(batchExecutor)
                .build();
    }
}

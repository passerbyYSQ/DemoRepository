package top.ysqorz.batch.springbatch.batch;

import cn.hutool.core.text.csv.*;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import top.ysqorz.batch.springbatch.model.vo.RowVO;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/20
 */
@Slf4j
public class RowItemDBWriter<T extends RowVO> implements ItemWriter<T> {
    private final SqlSessionFactory sqlSessionFactory;
    private final BiConsumer<T, SqlSession> consumer;
    private final File sourceCsv;
    private final Class<T> voClass;
    private StepExecution stepExecution;

    public RowItemDBWriter(SqlSessionFactory sqlSessionFactory, BiConsumer<T, SqlSession> consumer, File sourceCsv, Class<T> voClass) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.consumer = consumer;
        this.sourceCsv = sourceCsv;
        this.voClass = voClass;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            Map<String, String> errMsgMap = stepExecution.getExecutionContext().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
            writeResultCsv(errMsgMap);
        } catch (IOException e) {
            log.error("生成结果文件异常", e);
        }
        return stepExecution.getExitStatus();
    }

    @Override
    public void write(List<? extends T> items) {
        ExecutionContext context = stepExecution.getExecutionContext();
        for (T item : items) {
            // 每一行作为一个事务
            SqlSession sqlSession = sqlSessionFactory.openSession();
            String key = item.getRowKey();
            try {
                consumer.accept(item, sqlSession);
                sqlSession.commit();
                context.put(key, "成功");
            } catch (Exception ex) {
                sqlSession.rollback();
                context.put(key, ex.getMessage());
                log.error(ex.getMessage());
            } finally {
                sqlSession.close();
            }
        }
    }

    public void writeResultCsv(Map<String, String> errMsgMap) throws IOException {
        File resDir = new File(sourceCsv.getParentFile(), "res");
        if (!resDir.exists()) {
            if (!resDir.mkdirs()) {
                throw new IOException("创建目录失败：" + resDir.getAbsolutePath());
            }
        }
        CsvReadConfig csvReadConfig = new CsvReadConfig()
                .setHeaderLineNo(0)
                .setHeaderAlias(initHeaderAlias());
        File resultFile = new File(resDir, sourceCsv.getName());
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(sourceCsv.toPath()), StandardCharsets.UTF_8);
             CsvReader csvReader = CsvUtil.getReader(reader, csvReadConfig);
             CsvWriter writer = CsvUtil.getWriter(resultFile, StandardCharsets.UTF_8)) {
            CsvData csvData = csvReader.read();
            List<CsvRow> rows = csvData.getRows();
            writer.writeLine(csvData.getHeader().toArray(new String[0])); // 写入原表头
            for (CsvRow row : rows) {
                T rowVO = row.toBean(voClass);
                String err = errMsgMap.get(rowVO.getRowKey()); // 根据唯一标识提取错误信息
                if (ObjectUtil.isNotEmpty(err)) {
                    row.add(err);
                }
                writer.writeLine(row.getRawList().toArray(new String[0]));
            }
        }
    }

    public Map<String, String> initHeaderAlias() throws IOException {
        CsvReadConfig csvReadConfig = new CsvReadConfig().setHeaderLineNo(0);
        csvReadConfig.setEndLineNo(0); // 只读取表头
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(sourceCsv.toPath()), StandardCharsets.UTF_8);
             CsvReader csvReader = CsvUtil.getReader(reader, csvReadConfig)) {
            CsvData csvData = csvReader.read();
            List<String> header = csvData.getHeader();
            Field[] fields = voClass.getDeclaredFields();
            Map<String, String> headerAlias = new LinkedHashMap<>();
            for (int i = 0; i < header.size(); i++) {
                if (i >= fields.length) {
                    throw new IndexOutOfBoundsException("csv的列索引和vo类的属性定义不匹配：" + voClass.getName());
                }
                headerAlias.put(header.get(i), fields[i].getName()); // 顺序对应
            }
            return headerAlias;
        }
    }

}

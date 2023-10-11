package top.ysqorz.wpe;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.ObjectUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;
import org.springframework.util.ResourceUtils;
import top.ysqorz.util.CommonUtils;
import top.ysqorz.wpe.model.AttributeMeta;
import top.ysqorz.wpe.model.CsvRowVOMeta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void test() throws Exception {
        generateCsvRowVOJava(new File("E:\\Project\\ZW\\master\\sucore\\custom\\data_migration\\src\\main\\resources\\csv\\bo_Document_ALL_ALL.csv"));
    }

    public void generateCsvRowVOJava(File csvFile) throws Exception {
        CsvReadConfig csvReadConfig = new CsvReadConfig()
                .setHeaderLineNo(1)
                .setEndLineNo(1); // 只读取两行
        CsvRowVOMeta csvRowVOMeta = new CsvRowVOMeta();

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(csvFile.toPath()), StandardCharsets.UTF_8);
             CsvReader csvReader = CsvUtil.getReader(reader, csvReadConfig)) {
            CsvData csvData = csvReader.read();
            List<String> headerList = csvData.getHeader();
            List<String> aliasList = csvData.getRow(0).getRawList();
            List<AttributeMeta> attrMetaList = new ArrayList<>();
            for (int i = 0; i < headerList.size(); i++) {
                String header = headerList.get(i);
                if (ObjectUtil.isEmpty(header)) {
                    continue;
                }
//                String attr = StrUtil.lowerFirst(CommonUtils.toUpperCamelCase(header));
                String attr = CommonUtils.toUpperCamelCase(header);
                if (ObjectUtil.isEmpty(attr)) {
                    continue;
                }
                String alias = aliasList.get(i);
                attrMetaList.add(new AttributeMeta(attr, alias));
            }
            csvRowVOMeta.setAttrs(attrMetaList);
            String className = CommonUtils.toUpperCamelCase(FileUtil.getPrefix(csvFile));
            csvRowVOMeta.setClassName(className);
            csvRowVOMeta.setClassComment(csvFile.getName());
        }

        try (OutputStream outputStream = Files.newOutputStream(new File(csvFile.getParentFile(), csvRowVOMeta.getClassName() + ".java").toPath());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            config.setDirectoryForTemplateLoading(ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "template"));
            Template template = config.getTemplate("csv_row_vo.ftl");
            template.process(csvRowVOMeta, writer);
        }
    }
}

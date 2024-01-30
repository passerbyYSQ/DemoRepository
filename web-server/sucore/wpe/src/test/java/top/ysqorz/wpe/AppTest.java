package top.ysqorz.wpe;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.*;
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
import java.util.*;

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
        System.out.println(Convert.toInt("2.00001"));
    }

    @Test
    public void testGenerateCsvRowVO() throws Exception {
        generateCsvRowVOJava(new File("E:\\Project\\ZW\\master\\sucore\\custom\\data_migration\\src\\main\\resources\\csv\\rel-b2b_Classified Item.csv"));
    }

    @Test
    public void testFileSuffix() throws IOException {
        CsvReadConfig csvReadConfig = new CsvReadConfig()
                .setHeaderLineNo(0);
        File csvFile = new File("E:\\Project\\ZW\\master\\sucore\\custom\\data_migration\\src\\main\\resources\\csv\\bo_Document.csv");
        Set<String> suffixSet = new HashSet<>();
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(csvFile.toPath()), StandardCharsets.UTF_8);
             CsvReader csvReader = CsvUtil.getReader(reader, csvReadConfig)) {
            CsvData csvData = csvReader.read();
            for (int i = 0; i < csvData.getRowCount(); i++) {
                CsvRow row = csvData.getRow(i);
                List<String> rawList = row.getRawList();
                String filePath = rawList.get(rawList.size() - 1);
                suffixSet.add(FileUtil.getSuffix(filePath));
            }
        }
        System.out.println(suffixSet);
    }

    @Test
    public void testLock() {
        String lock1 = String.valueOf(1270381781);
        String lock2 = String.valueOf(1270381781);
        System.out.println(lock1.intern());
        System.out.println(lock2.intern());
    }

    @Test
    public void testPollQueue() {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(1);

        Integer poll1 = queue.poll();
        int poll2 = queue.poll();
        System.out.println(poll2);
    }
    public void generateCsvRowVOJava(File csvFile) throws Exception {
        int headerLine = 0;
        CsvReadConfig csvReadConfig = new CsvReadConfig()
                .setHeaderLineNo(headerLine)
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
                String alias = headerLine > 0 ? aliasList.get(i) : "";
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

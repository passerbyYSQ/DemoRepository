import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.*;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/20
 */
public class TestBatch {

    @Test
    public void testFileName() throws InterruptedException {
        File file = new File(new String("E:\\Project\\IdeaProjects\\DemoRepository\\web-client\\py\\csv\\bo_Document_ALL_ALL.csv"));
        File file2 = new File(new String("E:\\Project\\IdeaProjects\\DemoRepository\\web-client\\py\\csv\\bo_Document_ALL_ALL.csv"));
        String a = new String("123");
        String b = new String("123");
        System.out.println(a == b);
        System.out.println(a.intern() == b.intern());
        System.out.println(file.getAbsolutePath() == file2.getAbsolutePath());
        new Thread(() -> {
            synchronized (file.getAbsolutePath().intern()) {
                System.out.println("123");
                while (true) {

                }
            }
        }).start();
        Thread.sleep(2000);
        synchronized (file2.getAbsolutePath().intern()) {
            System.out.println("dgwwqg");
        }
    }

    @Test
    public void testReadCsv() {
        CsvReadConfig csvReadConfig = new CsvReadConfig()
                .setHeaderLineNo(0)
                .setTrimField(true)
                .setSkipEmptyRows(true);
        CsvReader reader = CsvUtil.getReader(csvReadConfig);
        CsvData csvData = reader.read(new File("E:\\Project\\IdeaProjects\\DemoRepository\\web-server\\spring-batch\\src\\main\\resources\\csv\\bo_Document_ALL_ALL.csv"));
        for (CsvRow row : csvData.getRows()) {
            List<String> rawList = row.getRawList();
            if (rawList.contains("PO_2022081601")) {
                System.out.println(rawList);
            }
        }
    }

    @Test
    public void test01() {
        System.out.println(FileUtil.getName("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\src\\test\\java\\top\\ysqorz\\i18n\\FileEventMonitorTest.java"));
    }
}

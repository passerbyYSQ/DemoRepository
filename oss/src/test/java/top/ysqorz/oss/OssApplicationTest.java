package top.ysqorz.oss;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.ysqorz.oss.strategy.IOSSStrategy;

import java.io.*;
import java.time.Duration;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
@SpringBootTest //获取启动类，加载配置，寻找主配置启动类 （被 @SpringBootApplication 注解的）
@RunWith(SpringRunner.class) //让JUnit运行Spring的测试环境,获得Spring环境的上下文的支持
@Slf4j
public class OssApplicationTest {
    @Autowired
    private IOSSStrategy ossStrategy;

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void testSimpleDownload() throws IOException {
        File file = new File("C:\\Users\\Administrator\\Desktop\\下载文件.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ossStrategy.simpleDownloadFileByAccessKey("demo/test.txt", fileOutputStream); // 注意不要/开头
        fileOutputStream.close();
    }

    @Test
    public void testSimpleUpload() throws IOException {
        File file = new File("C:\\Users\\Administrator\\Desktop\\下载文件.txt");
        FileInputStream fileInputStream = new FileInputStream(file);
        ossStrategy.simpleUploadFileByAccessKey("demo/test1.txt", fileInputStream); // 存在会覆盖
        fileInputStream.close();
    }

    @Test
    public void testGenerateSharedURL() {
        System.out.println(ossStrategy.generateSharedURL("demo/test.txt", Duration.ofMinutes(1)));
    }

}

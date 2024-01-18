import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/11/30
 */
public class TestImport {

    @Test
    public void simple2Traditional() {
        String traditional = ZhConverterUtil.toTraditional("欧阳");
        System.out.println(traditional);
        System.out.println(System.currentTimeMillis());
    }

    /**
     * [\u4e00-\u9fff]+=
     */
    @Test
    public void test() throws IOException {
        File srcFile = new File("E:\\Project\\ZW\\core_install\\ZWTeamworks\\config\\i18n\\WEB_messages_zh_CN.properties");
        File targetFile = new File("E:\\Project\\ZW\\core_install\\ZWTeamworks\\config\\i18n\\WEB_messages_zh_TW.properties");
        propValueSimple2Traditional(srcFile, targetFile);
    }

    public void propValueSimple2Traditional(File srcFile, File targetFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(srcFile.toPath()), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(targetFile, "UTF-8")) {
            String line;
            while (Objects.nonNull(line = reader.readLine())) {
                String[] split = line.split("=");
                if (split.length < 2) {
                    writer.println(line);
                } else {
                    String traditional = ZhConverterUtil.toTraditional(split[1]);
                    writer.println(split[0] + "=" + traditional);
                }
            }
        }
    }
}

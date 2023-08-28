package top.ysqorz.i18n;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Test;
import top.ysqorz.i18n.common.ConstInterfaceMeta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/28
 */
public class FreemarkerTest {
    @Test
    @SuppressWarnings("ConstantConditions")
    public void testTemplate() throws Exception {
        File templateDir = new File(ConstInterfaceMeta.class.getClassLoader().getResource("template").toURI());
        Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        config.setDirectoryForTemplateLoading(templateDir);
        Template template = config.getTemplate("const_interface.ftl");
        FileOutputStream outputStream = new FileOutputStream("E:\\Project\\IdeaProjects\\DemoRepository\\common\\i18n\\src\\main\\java\\top\\ysqorz\\i18n\\common\\TestConstInterface.java");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        ConstInterfaceMeta data = new ConstInterfaceMeta(ConstInterfaceMeta.class.getPackage().getName(), "TestConstInterface");
        data.addProps("MyORERootConfig", "MyORERootConfig");
        data.addProps("myUserName", "myUserName");
        data.addProps("my_good_friend", "my_good_friend");
        template.process(data, writer);
        writer.close();
    }
}

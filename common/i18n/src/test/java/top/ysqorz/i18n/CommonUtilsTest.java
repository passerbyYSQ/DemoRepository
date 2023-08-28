package top.ysqorz.i18n;

import org.junit.Assert;
import org.junit.Test;
import top.ysqorz.i18n.common.CommonUtils;

import java.io.File;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class CommonUtilsTest {
    @Test
    public void testGetJavaDir() {
        File javaDir = CommonUtils.getStandardJavaDirByClass(this.getClass());
        Assert.assertNotNull(javaDir);
        System.out.println(javaDir.getAbsolutePath());
        Assert.assertTrue(javaDir.exists());
    }

    @Test
    public void testClassPathResource() {
        File ftl = CommonUtils.getClassPathResource(this.getClass(), "template/const_interface.ftl");
        Assert.assertNotNull(ftl);
        System.out.println(ftl.getAbsolutePath());
        Assert.assertTrue(ftl.exists());
    }

    @Test
    public void testToUpperCamelCase() {
        Assert.assertEquals("MyRootConfig", CommonUtils.toUpperCamelCase("MY_ROOT_CONFIG"));
        Assert.assertEquals("MyRootConfig", CommonUtils.toUpperCamelCase("My_Root_Config"));
        Assert.assertEquals("MyRootConfig", CommonUtils.toUpperCamelCase("my_root_config"));
        Assert.assertEquals("MyRootConfig", CommonUtils.toUpperCamelCase("myRootConfig"));
        Assert.assertEquals("Myrootconfig", CommonUtils.toUpperCamelCase("myrootconfig"));
        Assert.assertEquals("MyRootConfig", CommonUtils.toUpperCamelCase("MyRootConfig"));
    }

    @Test
    public void testToScreamingSnake() {
        Assert.assertEquals("MY_ROOT_CONFIG", CommonUtils.toScreamingSnake("MyRootConfig"));
        Assert.assertEquals("MY_ORE_ROOT_CONFIG", CommonUtils.toScreamingSnake("MyORERootConfig"));
    }
}

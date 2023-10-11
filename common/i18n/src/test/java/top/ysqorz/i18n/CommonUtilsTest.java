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
    public void testRev() {
//        System.out.println(1 + 'A');
//        System.out.println('A' + 1);
        Assert.assertEquals("B", getNextRev("A"));
        Assert.assertEquals("E", getNextRev("D"));
        Assert.assertEquals("AA", getNextRev("Z"));
        Assert.assertEquals("AZ", getNextRev("AY"));
        Assert.assertEquals("BA", getNextRev("AZ"));
        Assert.assertEquals("CA", getNextRev("BZ"));
    }

    /**
     * A B C ... Z AA AB AC ... AZ BA BB BC ... BZ ... ... ... ZZ
     * <p>
     * 如果把A到B分别看成1到26，则上述可以看作类似26进制，之所以说是类似，因为没法表示0
     */
    public String getNextRev(String rev) {
        if (rev.isEmpty() || rev.length() > 2) {
            throw new RuntimeException("无效版本号：" + rev);
        }
        int revNum = revTo26BaseNum(rev);
        if (revNum >= revTo26BaseNum("ZZ")) {
            throw new RuntimeException("下一个版本号超出上限，最大的版本号为：ZZ，当前版本号：" + rev);
        }
        return revToStr(revNum + 1);
    }

    public String revToStr(int revNum) {
        StringBuilder revStr = new StringBuilder();
        // BZ => 2,26 => 2*26+26=78
        int temp = revNum;
        while (temp > 0) {
            temp -= 1; // 26进制是有0的，但是rev是没0的，从1开始算的，所以rev按照真正的26进制处理时要先减1
            char ch = (char) ((temp) % 26 + 'A');
            revStr.append(ch);
            temp /= 26;
        }
        return revStr.reverse().toString();
    }

    public int revTo26BaseNum(String rev) {
        int weight = 0;
        char[] chs = rev.toCharArray();
        for (char ch : chs) {
            weight = weight * 26 + (ch - 'A' + 1);
        }
        return weight;
    }

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
        Assert.assertEquals("I18nMessages", CommonUtils.toUpperCamelCase("i18n/messages"));
    }

    @Test
    public void testToScreamingSnake() {
        Assert.assertEquals("MY_ROOT_CONFIG", CommonUtils.toScreamingSnake("MyRootConfig"));
        Assert.assertEquals("MY_ORE_ROOT_CONFIG", CommonUtils.toScreamingSnake("MyORERootConfig"));
        Assert.assertEquals("LOGIN_USERNAME", CommonUtils.toScreamingSnake("login.username"));
    }
}

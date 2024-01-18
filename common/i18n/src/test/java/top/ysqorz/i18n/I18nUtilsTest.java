package top.ysqorz.i18n;

import org.junit.Assert;
import org.junit.Test;
import top.ysqorz.i18n.common.I18nUtils;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class I18nUtilsTest {

    @Test
    public void testLogFormat() {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                String methodName = record.getSourceMethodName();
                if (methodName.equalsIgnoreCase("loadAllCodes") || methodName.equals("verifyResourceBundleIntegrity")) {
                    return record.getMessage() + System.lineSeparator();
                } else {
                    return super.format(record);
                }
            }
        });
        Logger log = Logger.getLogger(I18nUtilsTest.class.getSimpleName());
        removeAllLoggerHandlers(log);
        log.addHandler(consoleHandler);
        log.info("123");
    }

    public void loadALLCodes(Logger log) {
    }

    public static void removeAllLoggerHandlers(Logger logger) {
        Logger curr = logger;
        while (Objects.nonNull(curr)) {
            Handler[] handlers = curr.getHandlers();
            for (Handler handler : handlers) {
                curr.removeHandler(handler);
            }
            curr = curr.getParent();
        }
    }

    @Test
    public void testCompute() {
        Map<String, Set<String>> map = new HashMap<>();
        map.computeIfAbsent("a", new Function<String, Set<String>>() {
            @Override
            public Set<String> apply(String s) {
                return new HashSet<>();
            }
        }).add("11111");
    }

    @Test
    public void testLocaleEqual() {
        Locale zh1 = Locale.forLanguageTag("zh-CN");
        Locale zh2 = Locale.SIMPLIFIED_CHINESE;

        System.out.println(zh1.equals(zh2));
    }

    @Test
    public void testLocale() {
        Locale zh1 = Locale.forLanguageTag("zh-CN");
        Locale zh2 = Locale.forLanguageTag("zh_CN");
        Locale root = Locale.ROOT;
        Locale def = Locale.getDefault();
        System.out.println(123);

        Map<String, Object> map = new HashMap<>();
        System.out.println(Locale.forLanguageTag(String.class.cast(map.get("a"))));
    }

    @Test
    public void testPattern() {
        String input = "hahaha\n|>>>xxxxx<<<|\n";
        String patternString = "\n\\|>>>(.*?)<<<\\|\n";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            // 获取匹配字符串的起始索引
            int startIndex = matcher.start();
            System.out.println("Start Index: " + startIndex);
            System.out.println("【"+ input.substring(0, startIndex) + "】");

            // 匹配的内容在 group(1) 中
//            String matchedText = matcher.group(1);
//            System.out.println("Matched Text: " + matchedText);
        }
    }

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
        File javaDir = I18nUtils.getStandardJavaDirByClass(this.getClass());
        Assert.assertNotNull(javaDir);
        System.out.println(javaDir.getAbsolutePath());
        Assert.assertTrue(javaDir.exists());
    }

    @Test
    public void testClassPathResource() {
        File ftl = I18nUtils.getClassPathResource(this.getClass(), "template/const_interface.ftl");
        Assert.assertNotNull(ftl);
        System.out.println(ftl.getAbsolutePath());
        Assert.assertTrue(ftl.exists());
    }

    @Test
    public void testToUpperCamelCase() {
        Assert.assertEquals("MyRootConfig", I18nUtils.toUpperCamelCase("MY_ROOT_CONFIG"));
        Assert.assertEquals("MyRootConfig", I18nUtils.toUpperCamelCase("My_Root_Config"));
        Assert.assertEquals("MyRootConfig", I18nUtils.toUpperCamelCase("my_root_config"));
        Assert.assertEquals("MyRootConfig", I18nUtils.toUpperCamelCase("myRootConfig"));
        Assert.assertEquals("Myrootconfig", I18nUtils.toUpperCamelCase("myrootconfig"));
        Assert.assertEquals("MyRootConfig", I18nUtils.toUpperCamelCase("MyRootConfig"));
        Assert.assertEquals("I18nMessages", I18nUtils.toUpperCamelCase("i18n/messages"));
    }

    @Test
    public void testToScreamingSnake() {
        Assert.assertEquals("MY_ROOT_CONFIG", I18nUtils.toScreamingSnake("MyRootConfig"));
        Assert.assertEquals("MY_ORE_ROOT_CONFIG", I18nUtils.toScreamingSnake("MyORERootConfig"));
        Assert.assertEquals("LOGIN_USERNAME", I18nUtils.toScreamingSnake("login.username"));
    }
}

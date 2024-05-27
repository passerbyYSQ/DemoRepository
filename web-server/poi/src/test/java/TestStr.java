import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.CheckedUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/2/2
 */
public class TestStr {
    public static final Pattern VAR_PATTERN = Pattern.compile("(.+)(_(\\d))?");


    @Test
    public void testDuration() {
        Set<Integer> set = new HashSet<>(Arrays.asList(3, 1, 4, 1, 5, 9));
        int[] array = set.stream()
                .sorted(Comparator.reverseOrder())
                .mapToInt(Integer::intValue)
                .toArray();
        System.out.println(Arrays.toString(array));
    }

    @Test
    public void testDecimalFormat() {

        System.out.println(NumberUtil.decimalFormat("#.##", new BigDecimal("1111126.455")));
    }
    @Test
    public void testRegex() {
        String[] s = "asvsav".split("_");
        System.out.println(s);

    }

    @Test
    public void testJson() {
        JSONObject entries = new JSONObject();
//        entries.set("obj", new JSONObject().set("osattrs", new JSONObject().set("children", new ArrayList<>())));
        Object byPath = entries.getByPath("obj.osattrs.children");
        System.out.println(byPath);
    }

    @Test
    public void testPlaceHolder() {
        System.out.println(NumberUtil.parseDouble("1"));
        System.out.println(Convert.toDouble("1"));
        System.out.println(Double.parseDouble("1"));

//        String type = Pattern.quote("input") + "|" + Pattern.quote("select");
//        Pattern pattern = Pattern.compile("\\{\\s*(" + type + ")\\s*}");
//        Matcher matcher = pattern.matcher("XXXXXx  { select } asvfgasd");
//        System.out.println(matcher.find());


//        String replace = Pattern.quote("设  设置位置${input}}；${");
//
////        String s = Matcher.quoteReplacement("设  设置位置${input}；");
////        System.out.println(s);
//
//        String str = "消防水池设置情况：${select}".replaceAll("\\$\\{([^{}]+)}", Matcher.quoteReplacement("设  设置位置${input}；"));
//        System.out.println(str);


//        System.out.println(StrUtil.trim("xxxx???", 0, new Predicate<Character>() {
//            @Override
//            public boolean test(Character character) {
//               return character == '?';
//            }
//        }));
    }

    @Test
    public void testBytes() {
        byte[] bytes = new byte[]{16, -1, 24, -45, 58, -76, -6, 13, 13, 10};
        System.out.println(new String(bytes, StandardCharsets.US_ASCII));
    }

    @Test
    public void testDp() {
        int n = 10; // n车数
        int m = 12; // 人数
        int k = 2; // 车容量

        int[][] dp = new int[n + 1][m + 1]; // [车数][人数]
        // 总人数只有1个，不管多少台车，多少容量，都只有一种坐法
        for (int i = 1; i <= k; i++) {
            dp[1][i] = 1;
        }
        for (int i = 2; i <= n; i++) { // 枚举n，车数
            for (int j = i; j <= m; j++) { // 枚举m，人数  m >= n
                int sum = 0;
                for (int x = 1; x <= k; x++) { // 枚举k   x <= k < j
                    if (j - x <= 0) {
                        continue;
                    }
                    sum += dp[i - 1][j - x];
                }
                dp[i][j] = sum;
            }
        }
        System.out.println(dp[n][m]);
    }

    @Test
    public void test2Str() {
        StringBuffer sbf = new StringBuffer();
        sbf.append("asf");
        sbf.append("112");
        System.out.println(String.valueOf(sbf));
    }

    @Test
    public void testDate() {
        DateTime dateTime = DateUtil.parseDateTime("2024-13-35");
        System.out.println(dateTime);
    }

    @Test
    public void testReplace() {
        String str = "我是 ${input} ，你好啊 ${xxxxx}";
        StrUtil.replace(str, "\\$\\{([^{}]+)}", new CheckedUtil.Func1Rt<Matcher, String>() {
            @Override
            public String call(Matcher parameter) throws RuntimeException {
                System.out.println(1);
                return null;
            }
        });
    }

    @Test
    public void test1() {
        System.out.println("1".getBytes(StandardCharsets.UTF_8).length);
        System.out.println("$".getBytes(StandardCharsets.UTF_8).length);
        System.out.println("你".getBytes(StandardCharsets.UTF_8).length);
        System.out.println("a".getBytes(StandardCharsets.UTF_8).length);
    }

}

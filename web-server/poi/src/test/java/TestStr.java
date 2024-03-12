import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.CheckedUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.regex.Matcher;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/2/2
 */
public class TestStr {
    @Test
    public void testPlaceHolder() {
//        Pattern pattern = Pattern.compile("\\$\\{([^{}]+)}");
//        Matcher matcher = pattern.matcher("XXXXXx  ${ input } asvfgasd");
//        System.out.println(matcher.find());

        System.out.println(StrUtil.trim("xxxx???", 0, new Predicate<Character>() {
            @Override
            public boolean test(Character character) {
               return character == '?';
            }
        }));
    }

    @Test
    public void testDp() {
        int n = 10; // n车数
        int m = 12; // 人数
        int k = 2; // 车容量

        int[][] dp =  new int[n + 1][m + 1]; // [车数][人数]
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

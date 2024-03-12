import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2024/2/4
 */
public class TestSort {

    @Test
    public void test1() {
        List<String> sections = Arrays.asList(
                "4.3.7-1",
                "3.1",
                "4.2.7",
                "4.1.7",
                "4.3.7",
                "4.2",
                "3.10.4-10",
                "3.10.4-2"
        );
        sortSections(sections, "[._-]");
        System.out.println(sections);
    }

    /**
     *  "4.2.7",
     *  "4.3.7",
     *  "4.3.7-1"
     */
    public static void sortSections(List<String> sections, String delimiterRegex) {
        sections.sort((s1, s2) -> {
            List<Integer> str1 = extractSectionNums(s1, delimiterRegex);
            List<Integer> str2 = extractSectionNums(s2, delimiterRegex);
            int size = Math.min(str1.size(), str2.size());
            for (int i = 0; i < size; i++) {
                Integer w1 = str1.get(i);
                Integer w2 = str2.get(i);
                if (!Objects.equals(w1, w2)) {
                    return Integer.compare(w1, w2);
                }
            }
            return Integer.compare(str1.size(), str2.size());
        });
    }

    /**
     * 根据正则表达式拆分字符串
     */
    public static List<Pair<TextType, String>> splitByPatternGroup(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        List<Pair<TextType, String>> parts = new ArrayList<>();
        int currIndex = 0;
        while (matcher.find()) {
            String placeHolder = matcher.group(0);
            int leftIndex = str.indexOf(placeHolder, currIndex); // 占位符左侧下标
            if (leftIndex > currIndex) {
                parts.add(Pair.of(TextType.NORMAL, str.substring(currIndex, leftIndex)));
            }
            parts.add(Pair.of(TextType.PLACEHOLDER, placeHolder));
            currIndex = leftIndex + placeHolder.length();
        }
        parts.add(Pair.of(TextType.NORMAL, str.substring(currIndex)));
        return parts;
    }

    public static List<Integer> extractSectionNums(String section, String delimiterRegex) {
        return Arrays.stream(section.split(delimiterRegex))
                .map(s -> Convert.toInt(StrUtil.trim(s)))
                .collect(Collectors.toList());
    }


    public enum TextType {
        NORMAL,
        PLACEHOLDER
    }

}

package top.ysqorz.TransmittableThreadLocal.demo;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 通过正则表达式实现SQL中*的作用
 */
public class ProcessAsteriskLikeSQL {
    @Test
    public void testTrim() {
        System.out.println(trim("*", '*').equals(""));
        System.out.println(trim("****", '*').equals(""));
        System.out.println(trim("**abc**", '*').equals("abc"));
        System.out.println(trim("abc**", '*').equals("abc"));
        System.out.println(trim("**abc", '*').equals("abc"));
        System.out.println(trim("*abc", '*').equals("abc"));
        System.out.println(trim("abc*", '*').equals("abc"));
    }

    @Test
    public void test() {
//        System.out.println(processAsteriskLikeSQL("****"));
//        System.out.println(processAsteriskLikeSQL("*"));
//        System.out.println(processAsteriskLikeSQL(""));
//        System.out.println(processAsteriskLikeSQL("ABC***123***"));
//        System.out.println(processAsteriskLikeSQL("ABC***123"));
//        System.out.println(processAsteriskLikeSQL("***ABC***123***"));
        match("ABC***123***", "ABC123", true);
        match("ABC***123***", "1234ABC123", false);
        match("+", "+", true);
        match("*", "badkg", true);
        match("**", "badkg", true);
        match("abc", "abcd", false);
        match("b", "ba", false);
        match("b*", "ba", true);
        match("b*", "aba", false);
        match("*b*", "aba", true);
        match("", "badkg", false);
        match("", "", true);
        match("+*+", "++", true);
        match("***A\\QBC12\\E3***", "1111A\\QBC12\\E3", true);
        match("A\\QBC12\\E3***", "1111A\\QBC12\\E3", false);
        match("A\\QBC12\\E3", "A\\QBC12\\E3", true);
        match("***A\\QBC12\\E3***", "A\\QBC123", false);
        match("*b*", "1111bbb", true);
    }

    public void match(String pattern, String word, boolean expected) {
        String regex = processAsteriskLikeSQL(pattern);
        boolean res = word.matches(regex);
        System.out.printf("[%s]\t[%s]\t[%s]\t[%b]%n", pattern, regex, word, res);
        assert res == expected;
    }

    public String processAsteriskLikeSQL(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return pattern;
        }
        final String POINT_ASTERISK = ".*";
        String trim = trim(pattern, '*');
        if (trim.isEmpty()) {
            return POINT_ASTERISK;
        }
        String prefix = pattern.charAt(0) == '*' ? POINT_ASTERISK : "";
        String suffix = pattern.charAt(pattern.length() - 1) == '*' ? POINT_ASTERISK : "";
        return Arrays.stream(trim.split("(\\*)+"))
                .map(Pattern::quote)
                .collect(Collectors.joining(POINT_ASTERISK, prefix, suffix));
    }

    public String trim(String str, char c) {
        char[] chs = str.toCharArray();
        int st = 0, end = chs.length;
        while (st < end && chs[st] == c) {
            st++;
        }
        while (st < end && chs[end - 1] == c) {
            end--;
        }
        return str.substring(st, end); // st <= end
    }
}

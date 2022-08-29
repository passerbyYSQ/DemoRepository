package top.ysqorz.demo.regex;

import org.junit.Test;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 通过正则表达式实现SQL中*的作用
 */
public class ProcessAsteriskLikeSQL {

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
        match("+*+", "++", true);
        match("***A\\QBC12\\E3***", "A\\QBC12\\E3", true);
        match("***A\\QBC12\\E3***", "A\\QBC123", false);
        match("*b*", "1111bbb", true);
    }

    public void match(String pattern, String word, boolean expected) {
        String regex = processAsteriskLikeSQL(pattern);
        boolean res = word.matches(regex);
        System.out.println(pattern + "\t" + word + "" + regex + "\t" + res);
        assert res == expected;
    }

    public String processAsteriskLikeSQL(String pattern) {
        // "" => ".*"
        if (pattern.isEmpty()) {
            return ".*";
        }
        String[] parts = trim(pattern, '*').split("(\\*)+");
        // "********" => ".*"
        if (parts.length == 0) {
            return ".*";
        }
        String prefix = pattern.charAt(0) == '*' ? ".*" : "";
        String suffix = pattern.charAt(pattern.length() - 1) == '*' ? ".*" : "";
        // ***ABC***123*** => [ABC, 123] => .*\QABC\E.*\Q123\E.*
        return Arrays.stream(parts).map(Pattern::quote).collect(Collectors.joining(".*", prefix, suffix));
    }

    public String trim(String str, char c) {
        char[] chs = str.toCharArray();
        int len = chs.length;
        int st = 0, end = len;
        while (st < len && chs[st] == c) {
            st++;
        }
        while (st < end && chs[end - 1] == c) {
            end--;
        }
        return st > 0 && end < len ? str.substring(st, end) : str;
    }

}

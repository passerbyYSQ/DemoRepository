package top.ysqorz.i18n.api.common;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class CommonUtils {
    public static String camelToScreamingSnake(String input) {
        StringBuilder result = new StringBuilder();
        char[] chs = input.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            result.append(Character.toUpperCase(chs[i]));
            // example: MyRootConfig => MY_ROOT_CONFIG
            if (i < input.length() - 1) {
                if (Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1]) &&
                        Character.isLowerCase(chs[i]) && Character.isUpperCase(chs[i + 1])) {
                    result.append("_");
                }
            }
            // example: MyORERootConfig => MY_ORE_ROOT_CONFIG
            if (i < input.length() - 2) {
                if (Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1]) && Character.isLetter(chs[i + 2]) &&
                        Character.isUpperCase(chs[i]) && Character.isUpperCase(chs[i + 1]) && Character.isLowerCase(chs[i + 2])) {
                    result.append("_");
                }
            }
        }
        return result.toString();
    }
}

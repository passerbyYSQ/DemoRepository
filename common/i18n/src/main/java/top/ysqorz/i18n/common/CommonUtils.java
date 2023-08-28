package top.ysqorz.i18n.common;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class CommonUtils {

    /**
     * => 大写驼峰
     */
    public static String toUpperCamelCase(String str) {
        StringBuilder result = new StringBuilder();
        char[] chs = str.toCharArray();
        for (int i = 0; i < chs.length; ) {
            if (i < chs.length - 1) {
                int idx = -2;
                if (i == 0 && Character.isLetter(chs[i])) {
                    idx = -1;
                }
                if (!Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1])) {
                    idx = i;
                }
                if (idx != -2) {
                    result.append(Character.toUpperCase(chs[idx + 1]));
                    int p = idx + 2;
                    while (p < chs.length && Character.isUpperCase(chs[p])) {
                        result.append(Character.toLowerCase(chs[p]));
                        p++;
                    }
                    i = p;
                    continue;
                }
            }
            // 其他没有任何可以区分单词的情况，保留本身字母
            result.append(chs[i]);
            i++;
        }
        return result.toString();
    }

    /**
     * 大小驼峰 => 大写蛇形命名 ABC_ROOT_CSA
     */
    public static String toScreamingSnake(String str) {
        StringBuilder result = new StringBuilder();
        char[] chs = str.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            result.append(Character.toUpperCase(chs[i]));
            // example: MyRootConfig => MY_ROOT_CONFIG
            if (i < str.length() - 1) {
                if (Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1]) &&
                        Character.isLowerCase(chs[i]) && Character.isUpperCase(chs[i + 1])) {
                    result.append("_");
                }
            }
            // example: MyORERootConfig => MY_ORE_ROOT_CONFIG
            if (i < str.length() - 2) {
                if (Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1]) && Character.isLetter(chs[i + 2]) &&
                        Character.isUpperCase(chs[i]) && Character.isUpperCase(chs[i + 1]) && Character.isLowerCase(chs[i + 2])) {
                    result.append("_");
                }
            }
        }
        return result.toString();
    }

    public static File getStandardJavaDirByClass(Class<?> clazz) {
        File srcDir = getStandardSourceDirByClass(clazz);
        if (Objects.isNull(srcDir)) {
            return null;
        }
        return new File(srcDir, String.join(File.separator, "main", "java"));
    }

    public static File getStandardSourceDirByClass(Class<?> clazz) {
         // ../target/classes/ZZXX.class
        URL resource = clazz.getClassLoader().getResource("./");
        if (Objects.isNull(resource)) {
            return null;
        }
        String uri = resource.toString();
        int idx = uri.lastIndexOf("target");
        if (idx < 0) {
            return null;
        }
        URI moduleURI = URI.create(uri.substring(0, idx));
        return new File(new File(moduleURI), "src");
    }

    public static File getClassPathResource(Class<?> clazz, String subPath) {
        URL resource = clazz.getClassLoader().getResource(subPath);
        if (Objects.isNull(resource)) {
            return null;
        }
        return new File(URI.create(resource.toString()));
    }

}

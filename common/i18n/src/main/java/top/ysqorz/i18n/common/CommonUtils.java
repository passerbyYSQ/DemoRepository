package top.ysqorz.i18n.common;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具类
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class CommonUtils {
    public static List<String> splitStr(String str, String delimiter) {
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(s -> !isEmpty(s))
                .collect(Collectors.toList());
    }

    /**
     * 使用指定分隔符，拼接字符串
     *
     * @param delimiter 分隔符
     * @param strs      需要拼接的多个字符串，可以为null
     * @return 拼接后的新字符串
     */
    public static String joinStr(String delimiter, String... strs) {
        if (isEmpty(strs)) {
            return "";
        }
        StringBuilder sbd = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (isEmpty(strs[i].trim())) {
                continue;
            }
            sbd.append(strs[i].trim());
            if (!isEmpty(sbd) && i < strs.length - 1 && !isEmpty(strs[i + 1])) {
                sbd.append(delimiter);
            }
        }
        return sbd.toString();
    }

    /**
     * 判断是否为空
     *
     * @param obj 传入判空对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj instanceof Optional) {
            return !((Optional<?>) obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else {
            return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
        }
    }

    public static boolean isRootLocale(Locale locale) {
        return Locale.ROOT.equals(locale);
    }

    public static boolean isLetterOrNum(char c) {
        return Character.isLetter(c) || Character.isDigit(c);
    }

    /**
     * 大写驼峰
     *
     * @param str 需处理字符串
     * @return 大写驼峰字符串
     */
    public static String toUpperCamelCase(String str) {
        StringBuilder result = new StringBuilder();
        char[] chs = str.toCharArray();
        // 前导大写超过两个，则保留
        int idx = 0;
        while (idx < chs.length && Character.isUpperCase(chs[idx])) {
            idx++;
        }
        if (idx > 1) {
            result.append(chs, 0, idx);
        } else {
            idx = 0;
        }
        for (int i = idx; i < chs.length; ) {
            if (i < chs.length - 1) {
                idx = -2;
                if (i == 0 && Character.isLetter(chs[i])) { // 首字母大写
                    idx = -1;
                }
                if (!isLetterOrNum(chs[i]) && isLetterOrNum(chs[i + 1])) { // 数字和字母算作一类
                    idx = i;
                }
                if (idx != -2) {
                    // example: My_Root_Config
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
            // 去除前导数字；去除连续的特殊字符
            // 其他没有任何可以区分单词的情况，如果是字母或数字，则保留本身
            if (result.length() > 0 && isLetterOrNum(chs[i])) {
                result.append(chs[i]);
            }
            i++;
        }
        return result.toString();
    }


    /**
     * 大写蛇形命名 ABC_ROOT_CSA
     *
     * @param str 需处理字符串
     * @return 蛇形命名字符串
     */
    public static String toScreamingSnake(String str) {
        StringBuilder result = new StringBuilder();
        char[] chs = str.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            // example: login.name => LOGIN_NAME
            if (isLetterOrNum(chs[i])) {
                result.append(Character.toUpperCase(chs[i]));
            }
            // example: MyRootConfig => MY_ROOT_CONFIG
            if (i < str.length() - 1) {
                if (Character.isLetter(chs[i + 1]) && Character.isUpperCase(chs[i + 1]) &&
                        !Character.isUpperCase(chs[i])) {
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

    /**
     * 通过类获取标准的Java文件夹路径
     *
     * @param clazz 获取路径的类
     * @return 所在文件夹
     */
    public static File getStandardJavaDirByClass(Class<?> clazz) {
        File srcDir = getStandardSourceDirByClass(clazz);
        if (Objects.isNull(srcDir)) {
            return null;
        }
        return new File(srcDir, joinStr(File.separator, "main", "java"));
    }

    /**
     * 通过类获取标准的源码文件路径
     *
     * @param clazz 获取路径的类
     * @return 所在类路径
     */
    public static File getStandardJavaFileByClass(Class<?> clazz) {
        File javaDir = CommonUtils.getStandardJavaDirByClass(clazz);
        if (Objects.isNull(javaDir)) {
            return null;
        }
        String subPath = joinStr("", clazz.getName().replace(".", File.separator), ".java");
        return new File(javaDir, subPath);
    }

    /**
     * 通过类获得标准的源码目录(src目录)
     *
     * @param clazz 获取路径的类
     * @return 所在文件夹
     */
    public static File getStandardSourceDirByClass(Class<?> clazz) {
        // .../target/classes/top/ysq/orz/model/User.class
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

    /**
     * 通过类以及指定路径获取资源文件
     *
     * @param clazz   获取路径的类
     * @param subPath 指定路径
     * @return 所在文件
     */
    public static File getClassPathResource(Class<?> clazz, String subPath) {
        URL resource = clazz.getClassLoader().getResource(subPath);
        if (Objects.isNull(resource)) {
            return null;
        }
        return new File(URI.create(resource.toString()));
    }

}

package top.ysqorz.i18n.common;

import tech.sucore.config.EnvironmentInfo;
import top.ysqorz.i18n.common.constant.I18nConstant;
import top.ysqorz.i18n.message.ConstInterfaceGenerator;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 工具类
 *
 * @author yaoshiquan
 * @date 2023/8/25
 */
public class I18nUtils {
    public static final Pattern I18N_DELIMITER_PATTERN = Pattern.compile("\n\\|>>>(.*?)<<<\\|\n");

    public static void generateConstInterfacesByCMD(ThrowableFunction<String[], ConstInterfaceGenerator> generatorFunc, String[] args) throws Exception {
        String lang = I18nUtils.getCmdArgs("-" + I18nConstant.PROPS_LANG, null, args);
        if (I18nUtils.isEmpty(lang)) {
            printArgsInfo();
            return;
        }
        try (ConstInterfaceGenerator generator = generatorFunc.apply(args)) {
            String pkgPath = I18nUtils.getCmdArgs("-p", I18nConstant.class.getPackage().getName(), args);
            String destDir = I18nUtils.getCmdArgs("-d", ".", args);
            Locale[] locales = Arrays.stream(lang.split(",")).map(String::trim).map(Locale::forLanguageTag).toArray(Locale[]::new);
            generator.generateConstInterfaces(pkgPath, new File(destDir), locales);
        }
    }

    public interface ThrowableFunction<T, R> {
        R apply(T t) throws Exception;
    }

    private static void printArgsInfo() {
        System.out.println("Args Options Description:");
        System.out.println("-i: Installation name. ex: default");
        System.out.println("-p: Package path for constant interface. ex: tech.sucore.common.constant");
        System.out.println("-d: The directory for generating constant interfaces. ex: D:\\temp");
        System.out.println("-lang: (*) Multiple languages involved in generating constant interfaces. ex: zh-CN,en-US");
    }

    public static File getStandardI18nDir() {
        return new File(EnvironmentInfo.getSystemConfigPath(), "i18n");
    }

    public static File getCustomI18nDir(String installation) {
        return new File(EnvironmentInfo.getInstallationConfigPath(installation), "i18n");
    }

    public static String subTranslatedFailMsg(String failMsg) {
        Matcher matcher = I18N_DELIMITER_PATTERN.matcher(failMsg);
        if (!matcher.find()) {
            return failMsg;
        }
        return failMsg.substring(0, matcher.start());
    }

    public static String getCmdArgs(String key, String defValue, String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].trim().equals(key.trim())) {
                if (i + 1 < args.length) {
                    return args[i + 1].trim();
                }
            }
        }
        return defValue;
    }

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
        int idx = 0;
        while (idx < strs.length) {
            String trimmed;
            if (isEmpty(strs[idx]) || isEmpty(trimmed = strs[idx].trim())) {
                idx++;
                continue;
            }
            sbd.append(trimmed);
            idx++;
            while (idx < strs.length && isEmpty(strs[idx]) && isEmpty(strs[idx].trim())) {
                idx++;
            }
            if (idx < strs.length) {
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
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
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
        int idx = 0;
        while (idx < chs.length) {
            int p = -2;
            if (idx == 0 && Character.isLetter(chs[idx])) { // 首字母大写
                p = -1;
            }
            if (idx < chs.length - 1 && !isLetterOrNum(chs[idx]) && isLetterOrNum(chs[idx + 1])) { // 数字和字母算作一类
                p = idx;
            }
            if (p != -2) {
                idx = p + 1;
                while (idx < chs.length && Character.isUpperCase(chs[idx])) {
                    idx++;
                }
                // 连续大写超过三个则保留 example: simple_UIService_impl => SimpleUIServiceImpl
                int cnt = idx - p - 1;
                if (cnt >= 3) {
                    result.append(chs, p + 1, cnt);
                    continue;
                }
                // example: My_Root_Config => MyRootConfig
                idx = p + 1; // 重置idx
                if (idx < chs.length) {
                    result.append(Character.toUpperCase(chs[idx]));
                    idx++;
                    while (idx < chs.length && Character.isUpperCase(chs[idx])) {
                        result.append(Character.toLowerCase(chs[idx]));
                        idx++;
                    }
                    continue;
                }
            }
            // 去除前导数字；去除连续的特殊字符
            // 其他没有任何可以区分单词的情况，如果是字母或数字，则保留本身
            if (idx < chs.length && result.length() > 0 && isLetterOrNum(chs[idx])) {
                result.append(chs[idx]);
            }
            idx++;
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
            // result.length() == 0 用于去除前导的数字
            if ((result.length() == 0 && Character.isLetter(chs[i])) || (result.length() > 0 && isLetterOrNum(chs[i]))) {
                result.append(Character.toUpperCase(chs[i]));
            }
            if (i < str.length() - 1) {
                // example: MyRootConfig => MY_ROOT_CONFIG
                if (Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1]) &&
                        Character.isLowerCase(chs[i]) && Character.isUpperCase(chs[i + 1])) {
                    result.append("_");
                }
                // example: login.name => LOGIN_NAME
                // result.length() > 0 用于去除前导的非字母
                if (result.length() > 0 && !Character.isLetter(chs[i]) && Character.isLetter(chs[i + 1])) {
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
        File javaDir = I18nUtils.getStandardJavaDirByClass(clazz);
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

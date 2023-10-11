package top.ysqorz.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CommonUtils {
    /**
     * 提取所有的请求参数，按照固定规则拼接成一个字符串
     *
     * @param body              post请求的请求体
     * @param paramMap          路径参数(QueryString)。形如：name=zhangsan&age=18&label=A&label=B
     * @param uriTemplateVarNap 路径变量(PathVariable)。形如：/{name}/{age}
     * @return 所有的请求参数按照固定规则拼接成的一个字符串
     */
    public static String extractRequestParams(@Nullable String body, @Nullable Map<String, String[]> paramMap,
                                              @Nullable Map<String, String> uriTemplateVarNap) {
        // body: { userID: "xxx" }

        // 路径参数
        // name=zhangsan&age=18&label=A&label=B
        // => ["name=zhangsan", "age=18", "label=A,B"]
        // => name=zhangsan&age=18&label=A,B
        String paramStr = null;
        if (!ObjectUtils.isEmpty(paramMap)) {
            paramStr = paramMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        // 拷贝一份按字典序升序排序
                        String[] sortedValue = Arrays.stream(entry.getValue()).sorted().toArray(String[]::new);
                        return entry.getKey() + "=" + joinStr(",", sortedValue);
                    })
                    .collect(Collectors.joining("&"));
        }

        // 路径变量
        // /{name}/{age} => /zhangsan/18 => zhangsan,18
        String uriVarStr = null;
        if (!ObjectUtils.isEmpty(uriTemplateVarNap)) {
            uriVarStr = joinStr(",", uriTemplateVarNap.values().stream().sorted().toArray(String[]::new));
        }

        // { userID: "xxx" }#name=zhangsan&age=18&label=A,B#zhangsan,18
        return joinStr("#", body, paramStr, uriVarStr);
    }

    /**
     * 使用指定分隔符，拼接字符串
     *
     * @param delimiter 分隔符
     * @param strs      需要拼接的多个字符串，可以为null
     * @return 拼接后的新字符串
     */
    public static String joinStr(String delimiter, @Nullable String... strs) {
        if (ObjectUtils.isEmpty(strs)) {
            return StrUtil.EMPTY;
        }
        StringBuilder sbd = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (ObjectUtils.isEmpty(strs[i])) {
                continue;
            }
            sbd.append(strs[i].trim());
            if (!ObjectUtils.isEmpty(sbd) && i < strs.length - 1 && !ObjectUtils.isEmpty(strs[i + 1])) {
                sbd.append(delimiter);
            }
        }
        return sbd.toString();
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return true; // ajax异步请求，返回json
        }
        String acceptStr = request.getHeader(HttpHeaders.ACCEPT);
        if (ObjectUtils.isEmpty(acceptStr)) {
            return true; // accept为空则默认返回json
        }
        List<MediaType> acceptList = MediaType.parseMediaTypes(acceptStr);
        MediaType jsonType = null, htmlType = null;
        for (MediaType mediaType : acceptList) {
            if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
                jsonType = mediaType;
            }
            if (MediaType.TEXT_HTML.equalsTypeAndSubtype(mediaType)) {
                htmlType = mediaType;
            }
        }
        if ((ObjectUtils.isEmpty(jsonType) && ObjectUtils.isEmpty(htmlType)) || ObjectUtils.isEmpty(htmlType)) {
            // 同时为空 或者 jsonType不为空且htmlType为空 时返回true
            return true;
        } else if (ObjectUtils.isEmpty(jsonType)) { // json为空，html不为空则返回html
            // jsonType为空且htmlType不为空 时返回false
            return false;
        } else {
            // jsonType和htmlType都不为空时，比较权重。如果jsonType的权重比htmlType的权重更大时，则返回true
            return Double.compare(jsonType.getQualityValue(), htmlType.getQualityValue()) > 0;
        }
    }

    // 拼接错误信息
    public static String joinErrorMsg(BindingResult bindingRes) {
        List<FieldError> fieldErrors = new ArrayList<>();
        List<ObjectError> otherErrors = new ArrayList<>();
        List<ObjectError> allErrors = bindingRes.getAllErrors();
        for (ObjectError objError : allErrors) {
            if (objError instanceof FieldError) {
                fieldErrors.add((FieldError) objError);
            } else {
                otherErrors.add(objError);
            }
        }
        Collector<CharSequence, ?, String> collector = Collectors.joining("; ");
        // FieldError
        String fieldErrorMsg = fieldErrors.stream()
                .map(fieldError -> fieldError.getField() + fieldError.getDefaultMessage())
                .collect(collector);
        // ObjectError
        String otherErrorMsg = otherErrors.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(collector);
        return Arrays.stream(new String[]{fieldErrorMsg, otherErrorMsg})
                .filter(s -> !ObjectUtils.isEmpty(s))
                .collect(collector);
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

}

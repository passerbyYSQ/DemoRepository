package top.ysqorz.signature.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Map;
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
}

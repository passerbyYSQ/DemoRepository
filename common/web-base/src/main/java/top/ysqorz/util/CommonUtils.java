package top.ysqorz.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CommonUtils {
    public static String joinStr(String delimiter, String... strs) {
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
}

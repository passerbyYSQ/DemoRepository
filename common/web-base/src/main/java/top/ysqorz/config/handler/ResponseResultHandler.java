package top.ysqorz.config.handler;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import top.ysqorz.common.annotation.NotWrapWithResultModel;
import top.ysqorz.common.dto.ResultModel;
import top.ysqorz.util.I18nUtils;
import top.ysqorz.util.JsonUtils;

/**
 * 对Controller层的方法返回值进行统一包装和国际化处理
 */
@RestControllerAdvice("top.ysqorz")
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (I18nUtils.isI18nOpen()) {
            // 如果开启了国际化，返回值不管是否为ResultModel，由于要进行多语言替换，都要返回true，以便处理结果
            return true;
        }
        // 由于没有开启国际化，返回值是ResultModel或者标注了@NotWrapWithResultModel注解，都无需处理结果
        return !(returnType.getParameterType().isAssignableFrom(ResultModel.class) // 兼容旧代码
                || returnType.hasMethodAnnotation(NotWrapWithResultModel.class)); // 提供灵活处理的钩子
    }

    @Override
    public Object beforeBodyWrite(Object res, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        return wrapResult(returnType, res, selectedConverterType, response);
    }

    public Object wrapResult(MethodParameter returnType, Object res,
                             Class<? extends HttpMessageConverter<?>> selectedConverterType,
                             ServerHttpResponse response) {
        ResultModel<?> result = res instanceof ResultModel ? (ResultModel<?>) res : ResultModel.success(res);
        result.setMsg(I18nUtils.translateMsg(result.getMsg().trim()));
        // 如果返回值不是String类型，则使用SpringBoot选择的转换器转换包裹对象
        if (!String.class.equals(returnType.getGenericParameterType())) {
            return result;
        }
        // 特殊处理返回值为String的情况，虽然selectedContentType为application/json，
        // 但是selectedConverterType却是StringHttpMessageConverter，导致包装成ResultModel后强转String报错
        // 所以需要在此处手动转json，StringHttpMessageConverter将json串强转成String时就不会报错了
        // https://zhuanlan.zhihu.com/p/413133915
        // returnType为void，res为null，也直接包装。但事实上连support()方法也不会进来
        // 如果返回值是String类型，但SpringBoot选择的转换器不是StringHttpMessageConverter，也使用SpringBoot选择的转换器转换包裹对象
        if (!selectedConverterType.isAssignableFrom(StringHttpMessageConverter.class)) {
            return result;
        }
        // 如果返回值是String类型，但SpringBoot选择的转换器是StringHttpMessageConverter，则需要手动转换为json写入到输出流
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON); // 设置json响应类型
        return JsonUtils.objToJson(result);
    }
}
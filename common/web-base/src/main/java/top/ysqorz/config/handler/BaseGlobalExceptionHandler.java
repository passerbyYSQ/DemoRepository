package top.ysqorz.config.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.dto.ResultModel;
import top.ysqorz.common.enumeration.StatusCode;
import top.ysqorz.exception.BaseException;
import top.ysqorz.util.CommonUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class BaseGlobalExceptionHandler {
    @Resource
    private MappingJackson2JsonView mappingJackson2JsonView;

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BaseException.class)
    public <T> ResultModel<T> BaseExceptionHandler(BaseException e, HttpServletResponse response) {
        response.setStatus(e.getHttpStatus().value()); // 动态设置响应的http状态码
        return ResultModel.failure(e.getIntegerCode(), e.getLocalizedMessage());
    }

    /**
     * 参数错误
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public <T> ResultModel<T> BindExceptionHandler(BindException e) {
        return ResultModel.failure(BaseStatusCode.PARAM_INVALID.getCode(), CommonUtils.joinErrorMsg(e.getBindingResult()));
    }

    /**
     * 参数错误
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public <T> ResultModel<T> ArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return ResultModel.failure(BaseStatusCode.PARAM_INVALID.getCode(), CommonUtils.joinErrorMsg(e.getBindingResult()));
    }

    /**
     * 参数错误
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public <T> ResultModel<T> handleConstraintViolationExceptionHandler(ConstraintViolationException e) {
        String errorMsg = e.getConstraintViolations().stream()
                .map(cvl -> {
                    String attr = cvl.getPropertyPath().toString().split("\\.")[1];
                    return attr + cvl.getMessage();
                })
                .collect(Collectors.joining("; "));
        return ResultModel.failure(BaseStatusCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 参数错误
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public <T> ResultModel<T> MissingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        return ResultModel.failure(BaseStatusCode.PARAM_INVALID.getCode(), e.getMessage());
    }

    /**
     * 参数错误
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public <T> ResultModel<T> HttpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        return ResultModel.failure(BaseStatusCode.PARAM_INVALID.getCode(), e.getMessage());
    }

    // 前后端联调时和正式上线后开启
    // 后端编码时，为了方便测试，先注释掉
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 非业务层面的异常，表示出现了服务端错误。
    @ExceptionHandler(Exception.class)
    public <T> ResultModel<T> ExceptionHandler(Exception e) {
        log.error(e.getMessage(), e); // 打印错误日志，方便调试
        return ResultModel.failure(BaseStatusCode.SERVER_ERROR.getCode(), e.getMessage());
    }

    protected ModelAndView wrapModelAndView(StatusCode code, String errorMsg, HttpServletRequest request) {
        ResultModel<Object> res = ObjectUtils.isEmpty(errorMsg) ?
                ResultModel.failure(code) :
                ResultModel.failure(code.getCode(), errorMsg); // errorMsg覆盖code的msg
        ModelAndView modelAndView = CommonUtils.isAjaxRequest(request) ?
                new ModelAndView(mappingJackson2JsonView) : // new MappingJackson2JsonView()
                new ModelAndView("error/500"); // 需要配置500页面的模板
        modelAndView.addObject("code", res.getCode())
                .addObject("msg", res.getMsg())
                .addObject("data", res.getData())
                .addObject("time", res.getTime());
        return modelAndView;
    }
}

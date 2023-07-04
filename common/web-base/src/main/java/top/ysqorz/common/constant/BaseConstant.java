package top.ysqorz.common.constant;

public interface BaseConstant {
    /**
     * JWT的请求头
     */
    String TOKEN_HEADER_AUTH = "Authorization";
    /**
     * 请求参数的签名的请求头
     */
    String X_REQUEST_SIGNATURE = "X-Request-Signature";
    /**
     * 调用方唯一标识的参数名
     */
    String PARAM_CALLER_ID = "callerID";
    /**
     * Token键名
     */
    String TOKEN_PARAMS_AUTH = "token";
}

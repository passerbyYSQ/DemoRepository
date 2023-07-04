package top.ysqorz.signature.model;

import top.ysqorz.common.constant.BaseStatusCode;
import top.ysqorz.common.enumeration.StatusCode;

import static top.ysqorz.common.enumeration.StatusCode.$;

public interface SignStatusCode extends BaseStatusCode {
    StatusCode UNTRUSTED_CALLER = $(4019, "不受信任的调用方");
    StatusCode REQUEST_SIGNATURE_INVALID = $(4006, "无效的请求参数签名");
}

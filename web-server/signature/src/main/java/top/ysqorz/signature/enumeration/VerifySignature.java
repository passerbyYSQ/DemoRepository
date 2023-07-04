package top.ysqorz.signature.enumeration;

import java.lang.annotation.*;

/**
 * 该注解标注于Controller类的方法上，表明该请求的参数需要校验签名
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface VerifySignature {
}

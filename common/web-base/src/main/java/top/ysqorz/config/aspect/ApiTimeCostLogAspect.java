package top.ysqorz.config.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import top.ysqorz.config.SpringContextHolder;

/**
 * 通用日志打印组件
 */
@Slf4j
@Aspect
@Component
public class ApiTimeCostLogAspect implements PointCutDef {

    @Around("apiMethod()")
    public Object apiMethodAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object res = null;
        Throwable ex = null;
        try {
            res = joinPoint.proceed();
        } catch (Throwable throwable) {
            ex = throwable;
        } finally {
            String methodName = extractMethodName(joinPoint);
            String servletPath = SpringContextHolder.getRequest().getServletPath();
            log.info("method: {}, path: {}, cost: {} ms", methodName, servletPath, System.currentTimeMillis() - start);
            // 如果发生了异常，继续往上抛，以便全局捕获
            if (!ObjectUtils.isEmpty(ex)) {
                throw ex;
            }
        }
        return res;
    }

    public String extractMethodName(ProceedingJoinPoint joinPoint) {
        String fullMethodName = joinPoint.getSignature().toString();
        int endIdx = fullMethodName.lastIndexOf("(");
        int startIdx = fullMethodName.lastIndexOf(" ", endIdx) + 1;
        return fullMethodName.substring(startIdx, endIdx);
    }
}

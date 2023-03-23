package top.ysqorz.TransmittableThreadLocal.config;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;

/**
 * <a href="http://events.jianshu.io/p/79650be29e34">...</a>
 */
public class MyRequestContextHolder  {
    private static final ThreadLocal<RequestAttributes> requestAttributesHolder = new NamedThreadLocal<>("Request attributes");

    private static final TransmittableThreadLocal<RequestAttributes> inheritableRequestAttributesHolder = new TransmittableThreadLocal<>();


    /**
     * Reset the RequestAttributes for the current thread.
     */
    public static void resetRequestAttributes() {
        requestAttributesHolder.remove();
        inheritableRequestAttributesHolder.remove();
    }

    /**
     * Bind the given RequestAttributes to the current thread,
     * <i>not</i> exposing it as inheritable for child threads.
     * @param attributes the RequestAttributes to expose
     * @see #setRequestAttributes(RequestAttributes, boolean)
     */
    public static void setRequestAttributes(@Nullable RequestAttributes attributes) {
        setRequestAttributes(attributes, false);
    }

    /**
     * Bind the given RequestAttributes to the current thread.
     * @param attributes the RequestAttributes to expose,
     * or {@code null} to reset the thread-bound context
     * @param inheritable whether to expose the RequestAttributes as inheritable
     * for child threads (using an {@link InheritableThreadLocal})
     */
    public static void setRequestAttributes(@Nullable RequestAttributes attributes, boolean inheritable) {
        if (attributes == null) {
            resetRequestAttributes();
        }
        else {
            if (inheritable) {
                inheritableRequestAttributesHolder.set(attributes);
                requestAttributesHolder.remove();
            }
            else {
                requestAttributesHolder.set(attributes);
                inheritableRequestAttributesHolder.remove();
            }
        }
    }

    /**
     * Return the RequestAttributes currently bound to the thread.
     * @return the RequestAttributes currently bound to the thread,
     * or {@code null} if none bound
     */
    @Nullable
    public static RequestAttributes getRequestAttributes() {
        RequestAttributes attributes = requestAttributesHolder.get();
        if (attributes == null) {
            attributes = inheritableRequestAttributesHolder.get();
        }
        return attributes;
    }
}

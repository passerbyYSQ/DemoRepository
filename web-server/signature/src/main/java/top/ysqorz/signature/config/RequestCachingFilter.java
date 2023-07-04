package top.ysqorz.signature.config;

import lombok.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import top.ysqorz.signature.model.SignatureProps;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ConditionalOnBean(SignatureProps.class)
@Component
public class RequestCachingFilter extends OncePerRequestFilter {
    /**
     * This {@code doFilter} implementation stores a request attribute for
     * "already filtered", proceeding without filtering again if the
     * attribute is already there.
     *
     * @param request     request
     * @param response    response
     * @param filterChain filterChain
     * @see #getAlreadyFilteredAttributeName
     * @see #shouldNotFilter
     * @see #doFilterInternal
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestWrapper = request;
        if (isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestWrapper = new ContentCachingRequestWrapper(request);
        }
        filterChain.doFilter(requestWrapper, response);
    }
}
package top.ysqorz.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "config.base", name = "log.rest.enabled", havingValue = "true", matchIfMissing = true)
public class RestTemplateLogger implements ClientHttpRequestInterceptor {

    @Value("${config.base.log.rest.excluded:}")
    private String[] excludedUrlSet;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uri = request.getURI().toString();
        if (isLog(uri, true)) {
            logRequest(request, body);
        }
        ClientHttpResponse response = execution.execute(request, body);
        if (isLog(uri, HttpStatus.OK.equals(response.getStatusCode()))) {
            logResponse(response);
        }
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.info("URI          : {}", request.getURI());
        log.info("Method       : {}", request.getMethod());
        log.info("Header       : {}", request.getHeaders());
        log.info("Request body : {}", new String(body, StandardCharsets.UTF_8));
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        StringBuilder sbd = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        String line = bufReader.readLine();
        while (line != null) {
            sbd.append(line);
            sbd.append(System.lineSeparator());
            line = bufReader.readLine();
        }
        log.info("Status code  : {}", response.getStatusCode());
        log.info("Header       : {}", response.getHeaders());
        log.info("Response body: {}", sbd);
    }

    public boolean isLog(String uri, boolean succeed) {
        if (ObjectUtils.isEmpty(excludedUrlSet)) {
            return true; // 没有被排除，则打印
        }
        for (String excludedUrl : excludedUrlSet) {
            if (uri.contains(excludedUrl)) {
                return !succeed; // 如果失败了，即使被排除也要打印
            }
        }
        return true; // 没有被排除
    }
}
package com.github.ulwx.aka.dbutils.springboot.resttemplate.interceptor;

import com.ulwx.tool.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {
    private static Logger log = LoggerFactory.getLogger(RestTemplateTokenRelayInterceptor.class);
    // 用来记录接口执行时间的最小接收值
    private final long timeoutMs;

    public LoggingRequestInterceptor(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        long start = System.currentTimeMillis();
        Exception exception=null;
        ClientHttpResponse response=null;
        long cost=-1;
        try {

            response = execution.execute(request, body);

            cost = System.currentTimeMillis() - start;
            if (cost > timeoutMs) {
                log.warn("Request uri: [{}], Cost times: {}ms", request.getURI(), cost);
            }
        }catch (Exception e){
            exception=e;
        } finally {
            // 打印日志
            try {
                log(request, body, response,exception,cost);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return response;

    }

    private void log(HttpRequest request, byte[] body,
                       ClientHttpResponse response,Exception e,long cost) throws Exception {
        // 记录日志
        String responseStr = IOUtils.toString(response.getBody(), "utf-8",false);
        log.info(
                "\n" + "URI          : {}, \n" +
                        "Method       : {}, \n" +
                        "Headers      : {}, \n" +
                        "body        : {}, \n" +
                        "RespStatus   : {}, \n" +
                        "Response     : {}, \n" +
                        "times(ms)   : {}, \n" ,
                request.getURI(),
                request.getMethod(), request.getHeaders(),
                new String(body, StandardCharsets.UTF_8),
                response.getStatusCode(), responseStr,
                cost);
        log.debug(e+"");
    }
}

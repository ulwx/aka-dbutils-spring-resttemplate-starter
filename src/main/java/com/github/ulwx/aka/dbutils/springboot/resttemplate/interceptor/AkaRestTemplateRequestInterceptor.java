package com.github.ulwx.aka.dbutils.springboot.resttemplate.interceptor;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class AkaRestTemplateRequestInterceptor implements ClientHttpRequestInterceptor {
    private RestTemplate restTemplate;
    private ApplicationContext applicationContext;
    public AkaRestTemplateRequestInterceptor(RestTemplate restTemplate, ApplicationContext applicationContext){
        this.restTemplate=restTemplate;
        this.applicationContext=applicationContext;
    }
    private static HashMap<String, String> defaultHeaders = new HashMap<String, String>();

    static {
        defaultHeaders.put("User-Agent", "Opera/9.80 (Windows NT 5.1; U; zh-cn) " + "Presto/2.2.15 Version/10.10");
        defaultHeaders.put("Accept",
                "text/html,application/json, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1");
        defaultHeaders.put("Accept-Language", "zh-cn,en;q=0.9,zh;q=0.8");
        defaultHeaders.put("Accept-Encoding", "identity, *;q=0");
        defaultHeaders.put("Accept-Charset", "utf-8,iso-8859-1, *;q=0.1");
        defaultHeaders.put("Content-Type", "text/html; charset=utf-8");
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.putIfAbsent("User-Agent", Arrays.asList(defaultHeaders.get("User-Agent")));
        headers.putIfAbsent("Accept-Language", Arrays.asList(defaultHeaders.get("Accept-Language")));
        headers.putIfAbsent("Accept-Encoding", Arrays.asList(defaultHeaders.get("Accept-Encoding")));
        headers.putIfAbsent("Accept-Charset", Arrays.asList(defaultHeaders.get("Accept-Charset")));
        headers.putIfAbsent("Content-Type", Arrays.asList(defaultHeaders.get("Content-Type")));

        return execution.execute(request, body);
    }
}

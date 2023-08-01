package com.github.ulwx.aka.dbutils.springboot.resttemplate;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.function.BiFunction;

public class HttpContextFactory implements BiFunction<HttpMethod, URI, HttpContext> {
    @Override
    public HttpContext apply(HttpMethod httpMethod, URI uri) {
        AkaRestContext.TimeOut timeOut=AkaRestContext.getTimeout();
        if (timeOut!=null) {
            RequestConfig requestConfig=RequestConfig.custom().
                    setSocketTimeout(timeOut.getSocketTimeout()).
                    setConnectionRequestTimeout(timeOut.getConnectTimeout()).
                    setConnectTimeout(timeOut.getConnectTimeout()).build();
            HttpContext context = HttpClientContext.create();
            context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
            return context;
        }
        return null;
    }
}

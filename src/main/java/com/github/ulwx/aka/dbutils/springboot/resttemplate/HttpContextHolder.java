package com.github.ulwx.aka.dbutils.springboot.resttemplate;

import org.apache.http.client.config.RequestConfig;
import org.springframework.core.NamedThreadLocal;

public class HttpContextHolder {

    private static final ThreadLocal<RequestConfig> threadLocal = new NamedThreadLocal<>("HTTP进程执行状态上下文");

    public static void bind(RequestConfig requestConfig) {
        threadLocal.set(requestConfig);
    }

    public static RequestConfig peek() {
        return threadLocal.get();
    }

    public static void unbind() {
        threadLocal.remove();
    }
}

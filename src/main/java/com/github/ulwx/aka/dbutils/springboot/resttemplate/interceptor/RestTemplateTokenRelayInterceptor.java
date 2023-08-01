package com.github.ulwx.aka.dbutils.springboot.resttemplate.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Map;


public class RestTemplateTokenRelayInterceptor implements ClientHttpRequestInterceptor {
    private static Logger log = LoggerFactory.getLogger(RestTemplateTokenRelayInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        MyHttpRequestWrapper myHttpRequestWrapper = new MyHttpRequestWrapper(request);
        //获取Token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest formRequest = sra.getRequest();
        this.setToken(formRequest, myHttpRequestWrapper);
        return execution.execute(myHttpRequestWrapper, body);
    }

    private void setToken(HttpServletRequest request, MyHttpRequestWrapper targetRequest) {

        Map<String, String> verifyInfo = (Map<String, String>) request.getAttribute("aka.jwt.vierify.info");
        if (verifyInfo == null) return ;
        String paramIn = verifyInfo.get("paramIn");
        String paraName = verifyInfo.get("paraName");
        boolean inHeader = false;
        boolean inQuery = false;
        boolean inCookie = false;
        if (!paramIn.isEmpty()) {
            paramIn = paramIn.trim();
            String[] strs = paramIn.split(",");
            for (int i = 0; i < strs.length; i++) {
                //header、query、cookie
                if (strs[i].equals("header")) inHeader = true;
                if (strs[i].equals("query")) inQuery = true;
                if (strs[i].equals("cookie")) inCookie = true;
            }
        } else {
            inHeader = true;
            inQuery = true;
            inCookie = true;
        }
        String token = "";
        if (inHeader) {
            token = request.getHeader(paraName);
            if (token == null) {
                token = "";
            }
            if(!token.isEmpty()) {
                HttpHeaders headers = targetRequest.getHeaders();
                headers.add(paraName, token);
            }
        }
        if (inQuery) {
            token = token.trim();
            if (token.isEmpty()) {//请求参数是否有Authorization
                token = request.getParameter(paraName);
                if (token == null) {
                    token = "";
                }
                if(!token.isEmpty()) {
                    targetRequest.setTokenParams(paraName, token);
                }

            }
        }
        if (inCookie) {
            token = token.trim();
            if (token.isEmpty()) { //cookie里是否有Authorization
                Cookie[] cookies = cookies = request.getCookies();
                if (cookies != null) {
                    for (int i = 0; i < cookies.length; i++) {
                        Cookie cookie = cookies[i];
                        if ((cookie.getName().equals(paraName))) {
                            token = cookie.getValue();
                            if (token == null) {
                                token = "";
                            }
                            if(!token.isEmpty()) {
                                HttpHeaders headers = targetRequest.getHeaders();
                                String originalCookie = headers.getFirst(HttpHeaders.COOKIE);
                                // 创建新的Cookie对象
                                ResponseCookie accesssTokenCookie = ResponseCookie.from(paraName, token).path("/").build();
                                // 将新的Cookie添加到请求头中
                                String modifiedCookie = "";
                                String appendCookie = accesssTokenCookie.toString();
                                if (originalCookie != null && !originalCookie.trim().isEmpty()) {
                                    modifiedCookie = originalCookie + "; " + appendCookie;

                                } else {
                                    modifiedCookie = appendCookie.toString();
                                }
                                targetRequest.getHeaders().add(HttpHeaders.COOKIE, modifiedCookie);
                            }
                        }
                    }

                }
            }
        }
    }


    public static class MyHttpRequestWrapper extends HttpRequestWrapper {
        private String paramName;
        private String paraValue;

        public MyHttpRequestWrapper(HttpRequest request) {
            super(request);
        }


        public void setTokenParams(String paramName, String paraValue) {
            this.paramName = paramName;
            this.paraValue = paraValue;
        }


        @Override
        public URI getURI() {
            if(paramName==null || paramName.trim().isEmpty()){
                return super.getURI();
            }
            URI uri = UriComponentsBuilder.fromHttpRequest(this.getRequest()).queryParam(paramName, paraValue).build().toUri();
            return uri;
        }
    }
}

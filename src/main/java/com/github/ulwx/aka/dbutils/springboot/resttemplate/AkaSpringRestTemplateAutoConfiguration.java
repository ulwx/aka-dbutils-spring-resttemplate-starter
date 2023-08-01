package com.github.ulwx.aka.dbutils.springboot.resttemplate;

import com.github.ulwx.aka.dbutils.springboot.resttemplate.interceptor.AkaRestTemplateRequestInterceptor;
import com.github.ulwx.aka.dbutils.springboot.resttemplate.interceptor.LoggingRequestInterceptor;
import com.github.ulwx.aka.dbutils.springboot.resttemplate.interceptor.RestTemplateTokenRelayInterceptor;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties(AkaRestTeplateProperties.class)
@PropertySource(value = {"classpath*:aka-application-rest.yml"},
        name="classpath*:aka-application-rest.yml",
        factory = MyPropertySourceFactory.class)
public class AkaSpringRestTemplateAutoConfiguration {
    private static Logger log = LoggerFactory.getLogger(AkaSpringRestTemplateAutoConfiguration.class);
    @Autowired
    private AkaRestTeplateProperties akaCloudConfigProperties;


    @ConditionalOnMissingBean
    @Bean
    AkaRestTemplateUtils restTemplateUtils(Map<String,RestTemplate> akaRestTemplate){
        return new AkaRestTemplateUtils(akaRestTemplate);
    }

    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean(name="akaForServiceRestTemplate")
    public RestTemplate akaForServiceRestTemplate(ApplicationContext applicationContext) {
        return createRestTemplate(applicationContext);
    }

    /**
     * 基于OkHttp3配置RestTemplate
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name="akaRestTemplate")
    public RestTemplate akaRestTemplate(ApplicationContext applicationContext) {
        return createRestTemplate(applicationContext);
    }

    public RestTemplate createRestTemplate(ApplicationContext applicationContext) {
        CloseableHttpClient httpClient = getClient(akaCloudConfigProperties.getRestTemplate().getPoolMaxConnections());
        HttpComponentsClientHttpRequestFactory httpsFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        httpsFactory.setReadTimeout(akaCloudConfigProperties.getRestTemplate().getSocketTimeoutSeconds()*1000);
        httpsFactory.setConnectTimeout(akaCloudConfigProperties.getRestTemplate().getConnectionTimeoutSeconds()*1000);
        httpsFactory.setConnectionRequestTimeout(akaCloudConfigProperties.getRestTemplate().getConnectionTimeoutSeconds()*1000);
        httpsFactory.setHttpContextFactory(new HttpContextFactory());
        RestTemplate restTemplate = new RestTemplate(httpsFactory);
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new AkaRestTemplateRequestInterceptor(restTemplate,applicationContext));
        interceptors.add(new RestTemplateTokenRelayInterceptor());
        interceptors.add(new LoggingRequestInterceptor(10000));
        // 可以添加消息转换
        //restTemplate.setMessageConverters(...);
        // 可以增加拦截器
        restTemplate.setInterceptors(interceptors);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(httpsFactory));
        return restTemplate;
    }


    private volatile static IdleConnectionMonitorThread connEvictor = null;
    private static CloseableHttpClient getClient(int maxTotalConnections) {

        try {
            // 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
                @Override public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override public void checkClientTrusted(X509Certificate[] xcs, String str) {}
                @Override public void checkServerTrusted(X509Certificate[] xcs, String str) {}
            };
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[] { trustManager }, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> registry =RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE).
                    register("https", socketFactory).build();

            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                    registry);

            connManager.setMaxTotal(maxTotalConnections);
            connManager.setDefaultMaxPerRoute(maxTotalConnections / 2);
            connManager.setValidateAfterInactivity(5000);
            LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();

            CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).
                    setRedirectStrategy(redirectStrategy).build();

            connEvictor = new IdleConnectionMonitorThread(connManager);
            connEvictor.setDaemon(true);
            connEvictor.start();
            return httpClient;
        } catch (Exception e) {
            log.error("", e);
        }

        return null;

    }

    public static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown=false;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {

                notifyAll();
            }
        }

    }
}

package com.github.ulwx.aka.dbutils.springboot.resttemplate;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aka.springboot")
public class AkaRestTeplateProperties {

    private RestTemplateInfo restTemplate=new RestTemplateInfo();

    public RestTemplateInfo getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplateInfo restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static class RestTemplateInfo{
        private Integer connectionTimeoutSeconds=10000;
        private Integer socketTimeoutSeconds=10000;
        private Integer poolMaxConnections=200;

        public Integer getConnectionTimeoutSeconds() {
            return connectionTimeoutSeconds;
        }

        public void setConnectionTimeoutSeconds(Integer connectionTimeoutSeconds) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
        }

        public Integer getSocketTimeoutSeconds() {
            return socketTimeoutSeconds;
        }

        public void setSocketTimeoutSeconds(Integer socketTimeoutSeconds) {
            this.socketTimeoutSeconds = socketTimeoutSeconds;
        }

        public Integer getPoolMaxConnections() {
            return poolMaxConnections;
        }

        public void setPoolMaxConnections(Integer poolMaxConnections) {
            this.poolMaxConnections = poolMaxConnections;
        }
    }
}

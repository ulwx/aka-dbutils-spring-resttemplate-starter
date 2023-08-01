package com.github.ulwx.aka.dbutils.springboot.resttemplate;

public class AkaRestContext {

    private static final ThreadLocal<TimeOut> timeoutHolder = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> serviceRequest = new ThreadLocal<>();

    public static void setServiceRequest(boolean isServiceRequest) {
        serviceRequest.set(isServiceRequest);
    }

    public static void removeServiceRequest(){
        serviceRequest.remove();
    }
    public static Boolean isServiceRequest(){
        return serviceRequest.get();
    }
    /**
     * 设置超时
     * @param timeout 单位秒
     */
    public static void setTimeout(int timeout) {
        TimeOut timeOut=new TimeOut(timeout,timeout);
        timeoutHolder.set(timeOut);
    }
    public static void setTimeout(Integer connectTimeout, Integer socketTimeout) {
        TimeOut timeOut=new TimeOut(connectTimeout,socketTimeout);
        timeoutHolder.set(timeOut);
    }
    public static TimeOut getTimeout(){
        return timeoutHolder.get();
    }
    public static void removeTimeout(){
        timeoutHolder.remove();
    }
    public static class TimeOut{
        private Integer connectTimeout;
        private Integer socketTimeout;

        public TimeOut(Integer connectTimeout, Integer socketTimeout) {
            this.connectTimeout = connectTimeout;
            this.socketTimeout = socketTimeout;
        }

        public Integer getSocketTimeout() {
            return socketTimeout;
        }

        public void setSocketTimeout(Integer socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }


    }
}

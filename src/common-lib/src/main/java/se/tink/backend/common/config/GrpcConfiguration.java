package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrpcConfiguration {
    @JsonProperty
    private int port = 9998;

    @JsonProperty
    private boolean enabled = false;


    @JsonProperty
    private boolean useTLS = false;


    @JsonProperty
    private String certPath = "";

    @JsonProperty
    private String keyPath = "";

    @JsonProperty
    private int keepAliveTime = 25;

    @JsonProperty
    private int keepAliveTimeout = 55;

    @JsonProperty
    private int maxConnectionAge = 60;

    @JsonProperty
    private int maxConnectionIdle = 3600;


    public int getPort() {
        return port;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isUseTLS() {
        return useTLS;
    }

    public String getCertPath() {
        return certPath;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public int getMaxConnectionAge() {
        return maxConnectionAge;
    }

    public int getMaxConnectionIdle() {
        return maxConnectionIdle;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setUseTLS(boolean useTLS) {
        this.useTLS = useTLS;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void setMaxConnectionAge(int maxConnectionAge) {
        this.maxConnectionAge = maxConnectionAge;
    }

    public void setMaxConnectionIdle(int maxConnectionIdle) {
        this.maxConnectionIdle = maxConnectionIdle;
    }
}

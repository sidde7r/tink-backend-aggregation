package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import java.util.HashMap;

public class SEBMortgageIntegrationConfiguration {
    @JsonProperty
    private Boolean https;
    @JsonProperty
    private String targetHost;
    @JsonProperty
    private HashMap<String, String> httpHeaders = Maps.newHashMap();
    @JsonProperty
    private String apiVersion;

    public boolean isHttps() {
        return https;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public HashMap<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}

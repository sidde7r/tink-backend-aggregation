package se.tink.backend.sms.gateways.cmtelecom.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CmTelecomConfig {
    @JsonProperty
    private String apiKey = null;

    @JsonProperty
    private String endpoint = "https://gw.cmtelecom.com/v1.0/message";

    public String getApiKey() {
        return apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetUrlResponse {
    private String targetUrl;

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}

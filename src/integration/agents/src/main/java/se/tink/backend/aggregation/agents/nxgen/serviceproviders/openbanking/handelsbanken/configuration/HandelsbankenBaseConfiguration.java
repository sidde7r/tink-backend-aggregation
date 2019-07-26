package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class HandelsbankenBaseConfiguration implements ClientConfiguration {

    @JsonProperty private String clientId;
    @JsonProperty private String appId;
    @JsonProperty private String psuIpAddress;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String appName;
    @JsonProperty private String appDesc;
    @JsonProperty private String certificateId;

    public String getClientId() {
        return clientId;
    }

    public String getAppId() {
        return appId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public String getCertificateId() {
        return certificateId;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class HandelsbankenBaseConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String tppId;
    @JsonProperty @Secret private String clientId;
    @JsonProperty @Secret private String psuIpAddress;
    @JsonProperty @Secret private String redirectUrl;
    @JsonProperty @Secret private String appName;
    @JsonProperty @Secret private String appDesc;
    @JsonProperty private String certificateId;

    public String getTppId() {
        return tppId;
    }

    public String getClientId() {
        return clientId;
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

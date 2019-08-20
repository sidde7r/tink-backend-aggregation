package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.configuration.Environment;

@JsonObject
public class IcaBankenConfiguration implements ClientConfiguration {

    @JsonProperty private String baseUrl;
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String certificateId;
    @JsonProperty private String environment = "PRODUCTION";

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getCertificateId() {
        return certificateId;
    }

    @JsonIgnore
    public Environment getEnvironment() {
        return Environment.fromString(environment);
    }
}

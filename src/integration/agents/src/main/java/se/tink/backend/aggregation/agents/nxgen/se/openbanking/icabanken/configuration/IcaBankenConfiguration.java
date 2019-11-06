package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.Environment;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class IcaBankenConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String clientId;
    @JsonProperty @SensitiveSecret private String clientSecret;
    @JsonProperty @Secret private String redirectUrl;
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

    @JsonIgnore
    public Environment getEnvironment() {
        return Environment.fromString(environment);
    }
}

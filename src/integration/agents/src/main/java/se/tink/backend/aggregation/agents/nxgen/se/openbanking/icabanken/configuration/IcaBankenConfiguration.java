package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IcaBankenConfiguration implements ClientConfiguration {

    @JsonProperty private String baseUrl;
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUri;
    @JsonProperty private String certificateId;
    @JsonProperty private String environment = "PRODUCTION";

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getCertificateId() {
        return certificateId;
    }

    @JsonIgnore
    public Environment getEnvironment() {
        return Environment.fromString(environment);
    }

    public enum Environment {
        SANDBOX("sandbox"),
        PRODUCTION("production");

        private final String value;

        Environment(String value) {
            this.value = value;
        }

        public static Environment fromString(String value) {
            return Environment.valueOf(value.toUpperCase());
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

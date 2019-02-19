package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class RabobankConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String clientSSLCertificate;
    @JsonProperty private String clientSSLKey;
    @JsonProperty private String clientSSLKeyPassword;
    @JsonProperty private String redirectUrl;

    //    @JsonIgnore
    //    private static final Logger logger = LoggerFactory.getLogger(ICSConfiguration.class);
    //    @JsonIgnore private static final LogTag MISSING_CONFIG =
    // LogTag.from("RABOBANK_MISSING_CONFIG");

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientSSLCertificate() {
        return clientSSLCertificate;
    }

    public String getClientSSLKey() {
        return clientSSLKey;
    }

    public String getClientSSLKeyPassword() {
        return clientSSLKeyPassword;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}

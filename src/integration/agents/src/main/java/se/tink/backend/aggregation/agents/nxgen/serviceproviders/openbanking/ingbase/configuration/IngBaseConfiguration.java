package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class IngBaseConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String baseUrl;
    @JsonProperty @Secret private String clientCertificate;
    @JsonProperty @AgentConfigParam private String redirectUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getClientCertificate() {
        return clientCertificate;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}

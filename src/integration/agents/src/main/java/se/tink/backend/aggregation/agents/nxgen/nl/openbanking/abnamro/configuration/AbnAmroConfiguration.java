package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class AbnAmroConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String clientId;

    @JsonProperty @SensitiveSecret private String apiKey;

    private String certificateId;

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCertificateId() {
        return certificateId;
    }
}

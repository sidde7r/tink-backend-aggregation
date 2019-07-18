package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class AISClientConfigurationEntity {

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String certificateId;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public URL getRedirectUrl() {
        return new URL(redirectUrl);
    }

    public String getCertificateId() {
        return certificateId;
    }
}

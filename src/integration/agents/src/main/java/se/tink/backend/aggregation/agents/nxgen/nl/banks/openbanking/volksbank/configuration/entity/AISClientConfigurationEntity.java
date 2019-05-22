package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AISClientConfigurationEntity {

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String clientCertificateContent;
    @JsonProperty private String clientCertificatePass;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientCertificateContent() {
        return clientCertificateContent;
    }

    public String getClientCertificatePass() {
        return clientCertificatePass;
    }
}

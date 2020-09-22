package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;

@JsonObject
public class Xs2aDevelopersProviderConfiguration {

    @JsonIgnore private String baseUrl;

    @Secret
    @JsonSchemaTitle("PSD ID")
    @JsonSchemaExamples("PSDSE-FINA-12345")
    @JsonSchemaDescription("PSD2 TPP number")
    private String clientId;

    @AgentConfigParam private String redirectUrl;

    public Xs2aDevelopersProviderConfiguration(
            String clientId, String baseUrl, String redirectUrl) {
        this.clientId = clientId;
        this.baseUrl = baseUrl;
        this.redirectUrl = redirectUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}

package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class ChebancaConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @Secret
    @JsonProperty
    @JsonSchemaTitle("Application Id")
    @JsonSchemaExamples("TEST_TPP_APP_01")
    @JsonSchemaDescription("Application Id registered in CheBanca portal")
    private String applicationId;

    public ChebancaConfiguration() {}

    public ChebancaConfiguration(String clientId, String clientSecret, String applicationId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}

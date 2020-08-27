package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.UUIDConfiguration;

@JsonObject
@Getter
public class AbancaConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;

    @SensitiveSecret
    @JsonSchemaTitle("Production API Key")
    @JsonSchemaDescription("The production API key for your App in the Abanca developer portal.")
    @UUIDConfiguration
    private String apiKey;
}

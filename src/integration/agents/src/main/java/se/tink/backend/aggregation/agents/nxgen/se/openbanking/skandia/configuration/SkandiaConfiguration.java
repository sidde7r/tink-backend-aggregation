package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
@Getter
public class SkandiaConfiguration implements ClientConfiguration {
    @Secret
    @ClientIdConfiguration
    @JsonSchemaExamples("4578616d706c652c2074657374696e67")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-f]{32}$")})
    private String clientId;

    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaExamples("4578616d706c652c2074657374696e67")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-f]{32}$")})
    private String clientSecret;
}

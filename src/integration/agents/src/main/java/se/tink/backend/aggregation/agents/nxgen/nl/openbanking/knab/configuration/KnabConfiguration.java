package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class KnabConfiguration implements ClientConfiguration {
    @JsonProperty
    @Secret
    @ClientIdConfiguration
    @JsonSchemaExamples("0123456789abcdefghij")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[-A-Za-z0-9]{10,30}$")})
    private String clientId;

    @JsonProperty
    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaExamples("4578616d706c652c2074657374696e67206d6f72")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[-A-Za-z0-9]{20,60}$")})
    private String clientSecret;
}

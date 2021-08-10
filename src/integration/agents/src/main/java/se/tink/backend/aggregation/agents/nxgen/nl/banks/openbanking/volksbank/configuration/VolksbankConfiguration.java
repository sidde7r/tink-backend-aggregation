package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration;

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
public class VolksbankConfiguration implements ClientConfiguration {
    @JsonProperty
    @Secret
    @ClientIdConfiguration
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^l[0-9a-f]+$")})
    @JsonSchemaExamples("l555d5513cb123456789050b90f06a18b2")
    private String clientId;

    @JsonProperty
    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-f]{32}$")})
    @JsonSchemaExamples("555d5513cb123456789050b90f06a18b")
    private String clientSecret;
}

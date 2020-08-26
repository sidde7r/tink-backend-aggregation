package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration;

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
public class LansforsakringarConfiguration implements ClientConfiguration {
    @Secret
    @ClientIdConfiguration
    @JsonSchemaExamples("lfbank-1abcd2eFghi3Jklm4opqR5sT")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[-_0-9A-Za-z]{30,60}$")})
    private String clientId;

    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaExamples("4578616d706c652c2074657374696e67206d6f72")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-f]{40}$")})
    private String clientSecret;
}

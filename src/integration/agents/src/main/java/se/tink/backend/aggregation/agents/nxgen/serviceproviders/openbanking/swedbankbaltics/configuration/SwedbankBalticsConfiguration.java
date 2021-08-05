package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.configuration;

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
import se.tink.backend.aggregation.configuration.agents.HexString32Configuration;

@JsonObject
@Getter
public class SwedbankBalticsConfiguration implements ClientConfiguration {

    @Secret
    @ClientIdConfiguration
    @JsonSchemaExamples("l4578616d706c652c2074657374696e671")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-z]{20,40}$")})
    private String clientId;

    @SensitiveSecret @ClientSecretsConfiguration @HexString32Configuration
    private String clientSecret;
}

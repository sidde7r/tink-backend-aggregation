package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.configuration;

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
import se.tink.backend.aggregation.configuration.agents.UUIDConfiguration;

@JsonObject
@Getter
public class EnterCardConfiguration implements ClientConfiguration {
    @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;

    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaExamples("WB1ZGjko-MvSIS2s1HhDOIhwss_7A6eeSjORr2IBR4L71rtoMTGo4sA9ACgEQKjM")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[-_A-Za-z0-9]{30,60}$")})
    private String clientSecret;
}

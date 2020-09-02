package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
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
public class SdcConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;

    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaExamples("WB1ZGjko-MvSIS2s1HhDOIhwss_7A6eeSjORr2IBR4L71rtoMTGo4sA9ACgEQKjM")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "^[-_A-Za-z0-9]{64}$")})
    private String clientSecret;

    @Secret
    @JsonSchemaDescription("The primary or secondary key from your SDC subscription.")
    @JsonSchemaTitle("API Subscription Key")
    @JsonSchemaExamples("737562736372697074696f6e206b6579")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-f]{32}$")})
    private String ocpApimSubscriptionKey;
}

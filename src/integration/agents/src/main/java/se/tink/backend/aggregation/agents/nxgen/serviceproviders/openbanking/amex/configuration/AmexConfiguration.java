package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
@Data
public class AmexConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonProperty(required = true)
    @JsonSchemaDescription("Server Url, will be deprecated")
    @JsonSchemaTitle("Server Url")
    @JsonSchemaInject(
            strings = {
                @JsonSchemaString(
                        path = "pattern",
                        value = "https:\\/\\/api2s\\.americanexpress\\.com")
            })
    @JsonSchemaExamples("https://api2s.americanexpress.com")
    @Secret
    private String serverUrl;

    @JsonProperty(required = true)
    @JsonSchemaDescription("GrantAccessJourney Url, will be deprecated")
    @JsonSchemaTitle("GrantAccessJourney Url")
    @JsonSchemaInject(
            strings = {@JsonSchemaString(path = "pattern", value = "https:\\/\\/m\\.amex\\/oauth")})
    @JsonSchemaExamples("https://m.amex/oauth")
    @Secret
    private String grantAccessJourneyUrl;
}

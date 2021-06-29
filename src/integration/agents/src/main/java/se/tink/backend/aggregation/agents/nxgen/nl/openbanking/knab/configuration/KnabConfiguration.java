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
    @JsonSchemaExamples("3edf42ds-2243-5421-8451-sedycGsFR31G")
    @JsonSchemaInject(
            strings = {
                @JsonSchemaString(
                        path = "pattern",
                        value =
                                "^[-A-Za-z0-9]{8}\\-[-A-Za-z0-9]{4}\\-[-A-Za-z0-9]{4}\\-[-A-Za-z0-9]{4}\\-[-A-Za-z0-9]{12}$")
            })
    private String clientId;

    @JsonProperty
    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaExamples("fds4!g@D64s5248%12gHcngrs677avw4982fhLn!")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[\\S+]{40}$")})
    private String clientSecret;
}

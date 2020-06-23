package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaBool;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.AgentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class NordeaBaseConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @JsonProperty @AgentConfigParam private String redirectUrl;

    @JsonProperty(required = true)
    @Secret
    @JsonSchemaTitle("Scopes")
    @JsonSchemaDescription(
            "These are the service scopes that PSD2 regulates. Payment initiation services (PIS) and account information services (AIS)")
    @JsonSchemaInject(
            bools = {@JsonSchemaBool(path = "uniqueItems", value = true)},
            ints = {
                @JsonSchemaInt(path = "minItems", value = 1),
                @JsonSchemaInt(path = "maxItems", value = 2)
            },
            json =
                    "{\n"
                            + "  \"items\" : {\n"
                            + "      \"enum\" : [\"AIS\",\"PIS\"]\n"
                            + "    }\n"
                            + "}")
    @JsonSchemaExamples("AIS")
    private List<String> scopes;

    @JsonProperty private AgentType agentType = AgentType.PERSONAL;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public List<String> getScopes() {
        Preconditions.checkNotNull(
                scopes,
                String.format(NordeaBaseConstants.ErrorMessages.INVALID_CONFIGURATION, "scopes"));
        Preconditions.checkArgument(
                !Iterables.isEmpty(scopes),
                String.format(NordeaBaseConstants.ErrorMessages.EMPTY_CONFIGURATION, "scopes"));
        return scopes;
    }
}

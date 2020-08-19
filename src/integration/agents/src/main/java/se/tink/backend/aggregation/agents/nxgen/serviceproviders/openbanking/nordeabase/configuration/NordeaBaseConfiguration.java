package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.ScopesConfiguration;

@JsonObject
public class NordeaBaseConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonProperty
    @Secret
    @ScopesConfiguration
    @JsonSchemaDescription(
            "These are the service scopes that PSD2 regulates: Payment initiation services (PIS) and account information services (AIS). This should not exceed your app's subscriptions on Nordea, or your PSD2 certification.")
    private List<String> scopes;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
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

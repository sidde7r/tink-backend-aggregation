package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.generators.nl.triodos.TriodosScope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
public class TriodosConfiguration implements BerlinGroupConfiguration {
    @JsonProperty
    @SensitiveSecret
    @ClientSecretsConfiguration
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "^[0-9a-zA-Z_-]+$")})
    private String clientSecret;

    @Override
    public String getClientId() {
        return null;
    }

    @Override
    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    // The below fields will be removed when BerlinGroup agent is refactored
    @Override
    public String getBaseUrl() {
        throw new NotImplementedException("The value is set in constant");
    }

    @Override
    public String getPsuIpAddress() {
        throw new NotImplementedException("The value is set in constant");
    }

    public static Set<TriodosScope> getTriodosScopes() {
        return Sets.newHashSet(
                TriodosScope.ACCOUNTS, TriodosScope.BALANCES, TriodosScope.TRANSACTIONS);
    }
}

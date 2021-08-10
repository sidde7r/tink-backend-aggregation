package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Scopes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.HexString32Configuration;
import se.tink.backend.aggregation.configuration.agents.ScopesConfiguration;

@NoArgsConstructor
@AllArgsConstructor
@JsonObject
@Getter
public class SkandiaConfiguration implements ClientConfiguration {
    @Secret @ClientIdConfiguration @HexString32Configuration private String clientId;

    @SensitiveSecret @ClientSecretsConfiguration @HexString32Configuration
    private String clientSecret;

    @Secret
    @ScopesConfiguration
    @JsonSchemaDescription(
            "These are the service scopes that PSD2 regulates: Payment initiation services (PIS) and account information services (AIS). This should not exceed your app's subscriptions on Skandia, or your PSD2 certification. AIS scope is always added by default.")
    private Set<String> scopes;

    public Set<String> getScopes() {

        if (CollectionUtils.isEmpty(scopes)) {
            return Collections.singleton(Scopes.AIS);
        }

        Set<String> scopesWithDefaultValue = new HashSet<>(scopes);
        scopesWithDefaultValue.add(Scopes.AIS);
        return scopesWithDefaultValue;
    }
}

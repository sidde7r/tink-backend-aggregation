package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.ScopesConfiguration;

@JsonObject
@Getter
public class NordeaBaseConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @Secret
    @ScopesConfiguration
    @JsonSchemaDescription(
            "These are the service scopes that PSD2 regulates: Payment initiation services (PIS) and account information services (AIS). This should not exceed your app's subscriptions on Nordea, or your PSD2 certification.")
    private List<String> scopes;
}

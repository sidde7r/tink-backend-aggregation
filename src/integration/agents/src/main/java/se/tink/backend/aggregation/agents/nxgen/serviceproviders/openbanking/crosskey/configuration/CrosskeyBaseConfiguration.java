package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
@Getter
public class CrosskeyBaseConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
}

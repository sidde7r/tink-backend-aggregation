package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.HexString32Configuration;

@JsonObject
@Getter
public class SkandiaConfiguration implements ClientConfiguration {
    @Secret @ClientIdConfiguration @HexString32Configuration private String clientId;

    @SensitiveSecret @ClientSecretsConfiguration @HexString32Configuration
    private String clientSecret;
}

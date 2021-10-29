package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

@JsonObject
@Getter
public class ArkeaConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientKey;
    @SensitiveSecret private String clientSecret;
    @Secret private String qsealcUrl;
}

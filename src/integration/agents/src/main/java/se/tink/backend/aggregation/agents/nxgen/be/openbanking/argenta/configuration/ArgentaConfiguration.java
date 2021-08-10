package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@Getter
@JsonObject
public class ArgentaConfiguration implements ClientConfiguration {

    @Secret private String clientId;

    @SensitiveSecret private String apiKey;
}

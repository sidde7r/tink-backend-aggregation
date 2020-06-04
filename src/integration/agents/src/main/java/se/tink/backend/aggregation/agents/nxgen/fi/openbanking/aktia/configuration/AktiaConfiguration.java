package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration;

import lombok.Data;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Data
public class AktiaConfiguration implements ClientConfiguration {

    @Secret private String apiServerUrl;

    @Secret private String authServerUrl;

    @SensitiveSecret private String basicAuthHeaderValue;
}

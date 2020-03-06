package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration;

import lombok.Data;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Data
public class AmexConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;

    @AgentConfigParam private String redirectUrl;
    @Secret private String serverUrl;
    @Secret private String grantAccessJourneyUrl;
}

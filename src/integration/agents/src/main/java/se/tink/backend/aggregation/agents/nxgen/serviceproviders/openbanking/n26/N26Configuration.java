package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
public class N26Configuration implements ClientConfiguration {

    @SensitiveSecret private String apiKey;
    @SensitiveSecret private String memberId;
    @SensitiveSecret private String realmId;
    @AgentConfigParam private String redirectUrl;
    @Secret private String baseUrl;
    @Secret private String authorizationUrl;
    @Secret private String aliasType;
    @Secret private String aliasValue;
}

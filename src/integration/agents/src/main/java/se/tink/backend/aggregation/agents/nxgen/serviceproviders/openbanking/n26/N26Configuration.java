package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class N26Configuration implements ClientConfiguration {

    @SensitiveSecret private String apiKey;
    @SensitiveSecret private String memberId;
    @SensitiveSecret private String realmId;
    @AgentConfigParam private String redirectUrl;
}

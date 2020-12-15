package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.refresh_token;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class N26RefreshTokenParameters {
    private final String clientId;
    private final String baseUrl;
}

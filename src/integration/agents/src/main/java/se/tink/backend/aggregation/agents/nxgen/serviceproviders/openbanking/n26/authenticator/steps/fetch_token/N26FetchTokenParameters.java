package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class N26FetchTokenParameters {

    private final String clientId;
    private final String redirectUrl;
    private final String baseUrl;
}

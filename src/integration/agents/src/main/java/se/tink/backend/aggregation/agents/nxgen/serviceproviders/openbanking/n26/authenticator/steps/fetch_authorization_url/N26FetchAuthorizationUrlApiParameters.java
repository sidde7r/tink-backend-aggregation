package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class N26FetchAuthorizationUrlApiParameters {
    private final String baseUrl;
    private final String scope;
}

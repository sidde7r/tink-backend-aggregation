package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.authorization_redirect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilder;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilderAuthenticationParameters;

@RequiredArgsConstructor
public class N26RedirectUrlBuilder implements RedirectUrlBuilder {

    private final ObjectMapper objectMapper;

    @Override
    public String createAuthorizationUrl(RedirectUrlBuilderAuthenticationParameters parameters) {
        return new N26ProcessStateAccessor(parameters.getAuthenticationProcessState(), objectMapper)
                .getN26ProcessStateData()
                .getAuthorizationUri()
                .toString();
    }
}

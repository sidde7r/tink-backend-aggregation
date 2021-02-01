package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.refresh_token;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2RefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;

public class N26OAuth2RefreshTokenCall extends OAuth2RefreshTokenCall {

    private final N26RefreshTokenParameters refreshTokenParameters;

    public N26OAuth2RefreshTokenCall(
            AgentHttpClient httpClient, N26RefreshTokenParameters parameters) {
        super(httpClient);
        this.refreshTokenParameters = parameters;
    }

    @Override
    protected Map<String, String> getClientSpecificHeaders(
            RedirectRefreshTokenCallAuthenticationParameters parameters) {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, String> getClientSpecificParams(
            RedirectRefreshTokenCallAuthenticationParameters parameters) {
        return Collections.emptyMap();
    }

    @Override
    protected String getClientId(RedirectRefreshTokenCallAuthenticationParameters parameters) {
        return refreshTokenParameters.getClientId();
    }

    @Override
    protected URI getRefreshTokenEndpoint() {
        return URI.create(
                refreshTokenParameters.getBaseUrl()
                        + Url.TOKEN
                        + refreshTokenParameters.getScope());
    }
}

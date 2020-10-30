package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2RefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;

public class KbcRefreshTokenCall extends OAuth2RefreshTokenCall {

    private final KbcConfiguration kbcConfiguration;

    public KbcRefreshTokenCall(AgentHttpClient httpClient, KbcConfiguration kbcConfiguration) {
        super(httpClient);
        this.kbcConfiguration = kbcConfiguration;
    }

    @Override
    protected Map<String, String> getClientSpecificHeaders(
            RedirectRefreshTokenCallAuthenticationParameters input) {
        return TokenEndpointSpecificationProvider.getClientSpecificHeaders(kbcConfiguration);
    }

    @Override
    protected Map<String, String> getClientSpecificParams(
            RedirectRefreshTokenCallAuthenticationParameters input) {
        Map<String, String> params = new HashMap<>();
        params.put(BerlinGroupConstants.QueryKeys.CLIENT_ID, getClientId(input));
        return params;
    }

    @Override
    protected String getClientId(RedirectRefreshTokenCallAuthenticationParameters input) {
        return kbcConfiguration.getClientId();
    }

    @Override
    protected URI getRefreshTokenEndpoint() {
        return TokenEndpointSpecificationProvider.getAccessTokenEndpoint();
    }
}

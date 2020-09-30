package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.OAuth2RedirectFetchTokenCall;
import se.tink.backend.aggregation.agentsplatform.framework.http.HttpClient;

public class KbcFetchTokenCall extends OAuth2RedirectFetchTokenCall {

    private final KbcConfiguration kbcConfiguration;
    private final URI redirectUrl;
    private final KbcPersistedDataAccessorFactory persistedDataAccessorFactory;

    public KbcFetchTokenCall(
            HttpClient httpClient,
            KbcConfiguration kbcConfiguration,
            URI redirectUrl,
            KbcPersistedDataAccessorFactory persistedDataAccessorFactory) {
        super(httpClient);
        this.kbcConfiguration = kbcConfiguration;
        this.redirectUrl = redirectUrl;
        this.persistedDataAccessorFactory = persistedDataAccessorFactory;
    }

    @Override
    protected Map<String, String> getClientSpecificHeaders(
            RedirectFetchTokenCallAuthenticationParameters input) {
        return TokenEndpointSpecificationProvider.getClientSpecificHeaders(kbcConfiguration);
    }

    @Override
    protected Map<String, String> getClientSpecificParams(
            RedirectFetchTokenCallAuthenticationParameters input) {
        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put(
                "code_verifier",
                persistedDataAccessorFactory
                        .createKbcAuthenticationPersistedDataAccessor(
                                input.getAuthenticationPersistedData())
                        .getKbcAuthenticationData()
                        .getCodeVerifier());
        return additionalParams;
    }

    @Override
    protected URI getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    protected String getClientId(RedirectFetchTokenCallAuthenticationParameters input) {
        return kbcConfiguration.getClientId();
    }

    @Override
    protected URI getAccessTokenEndpoint() {
        return TokenEndpointSpecificationProvider.getAccessTokenEndpoint();
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingAisAuthenticator implements OpenIdAuthenticator {

    private final UkOpenBankingApiClient apiClient;

    public UkOpenBankingAisAuthenticator(UkOpenBankingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        String intentId = apiClient.createConsent();

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();
        AuthorizeRequest.Builder authorizeRequestBuilder =
                AuthorizeRequest.create().withAccountsScope();
        if (wellKnownConfiguration.supportsScope(Scopes.OFFLINE_ACCESS)) {
            authorizeRequestBuilder.withAdditionalScopeValue(Scopes.OFFLINE_ACCESS);
        }

        return authorizeUrl.queryParam(
                OpenIdAuthenticatorConstants.Params.REQUEST,
                authorizeRequestBuilder
                        .withClientInfo(apiClient.getProviderConfiguration())
                        .withSoftwareStatement(apiClient.getSoftwareStatement())
                        .withRedirectUrl(apiClient.getRedirectUrl())
                        .withState(state)
                        .withNonce(nonce)
                        .withCallbackUri(callbackUri)
                        .withWellKnownConfiguration(wellKnownConfiguration)
                        .withIntentId(intentId)
                        .withMaxAge(OpenIdAuthenticatorConstants.MAX_AGE)
                        .build(apiClient.getSigner()));
    }
}

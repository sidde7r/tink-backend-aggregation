package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.newday.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.newday.NewDayConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NewDayAisAuthenticator implements OpenIdAuthenticator {

    private final UkOpenBankingApiClient apiClient;

    public NewDayAisAuthenticator(UkOpenBankingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        String intentId = apiClient.createConsent();

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(
                OpenIdAuthenticatorConstants.Params.REQUEST,
                AuthorizeRequest.create()
                        .withAccountsScope()
                        .withAdditionalScopeValue(NewDayConstants.OFFLINE_ACCESS)
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

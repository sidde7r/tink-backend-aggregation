package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.AuthorizeRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UkOpenBankingAisAuthenticator implements OpenIdAuthenticator {

    private final UkOpenBankingApiClient apiClient;
    private final ProviderConfiguration providerConfiguration;

    public UkOpenBankingAisAuthenticator(UkOpenBankingApiClient apiClient) {
        this.apiClient = apiClient;
        this.providerConfiguration = apiClient.getProviderConfiguration();
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        String intentId = apiClient.fetchIntentIdString();

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(
                UkOpenBankingAisAuthenticatorConstants.Params.REQUEST,
                AuthorizeRequest.create()
                        .withAccountsScope()
                        .withClientInfo(providerConfiguration.getClientInfo())
                        .withSoftwareStatement(apiClient.getSoftwareStatement())
                        .withRedirectUrl(apiClient.getRedirectUrl())
                        .withState(state)
                        .withNonce(nonce)
                        .withCallbackUri(callbackUri)
                        .withWellknownConfiguration(wellKnownConfiguration)
                        .withIntentId(intentId)
                        .build(apiClient.getSigner()));
    }

    @Override
    public ClientMode getClientCredentialScope() {
        return ClientMode.ACCOUNTS;
    }
}

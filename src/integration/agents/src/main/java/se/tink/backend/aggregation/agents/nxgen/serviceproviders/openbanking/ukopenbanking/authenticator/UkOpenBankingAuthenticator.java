package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.jwt.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingAuthenticator implements OpenIdAuthenticator {

    private final UkOpenBankingApiClient apiClient;
    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;

    public UkOpenBankingAuthenticator(UkOpenBankingApiClient apiClient) {
        this.apiClient = apiClient;
        this.softwareStatement = apiClient.getSoftwareStatement();
        this.providerConfiguration = apiClient.getProviderConfiguration();
    }

    @Override
    public URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce) {
        AccountPermissionResponse accountPermissionResponse = apiClient.createAccountIntentId();
        String intentId = accountPermissionResponse.getData().getAccountRequestId();

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(UkOpenBankingAuthenticatorConstants.Params.REQUEST,
                AuthorizeRequest.create()
                        .withAccountsScope()
                        .withClientInfo(providerConfiguration.getClientInfo())
                        .withSoftwareStatement(softwareStatement)
                        .withState(state)
                        .withNonce(nonce)
                        .withWellknownConfiguration(wellKnownConfiguration)
                        .withIntentId(intentId)
                        .build()
        )
                .queryParam("authondevice","YES");
    }
}

package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.authenticator;

import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BarclaysV31AisAuthenticator implements OpenIdAuthenticator {

    private final UkOpenBankingApiClient apiClient;
    private final ClientInfo clientInfo;
    private final Set<String> permissions;

    public BarclaysV31AisAuthenticator(UkOpenBankingApiClient apiClient, Set<String> permissions) {
        this.apiClient = apiClient;
        this.clientInfo = apiClient.getProviderConfiguration();
        this.permissions = permissions;
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        String intentId = apiClient.createConsent(permissions);

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl
                .queryParam(BarclaysConstants.AUTH_ON_DEVICE, "YES")
                .queryParam(
                        OpenIdAuthenticatorConstants.Params.REQUEST,
                        AuthorizeRequest.create()
                                .withAccountsScope()
                                .withClientInfo(clientInfo)
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

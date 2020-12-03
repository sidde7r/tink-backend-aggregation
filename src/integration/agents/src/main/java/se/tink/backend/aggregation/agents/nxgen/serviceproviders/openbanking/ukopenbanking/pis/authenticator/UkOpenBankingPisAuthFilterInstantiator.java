package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class UkOpenBankingPisAuthFilterInstantiator {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final OpenIdAuthenticationValidator authenticationValidator;

    public void instantiateAuthFilterWithClientToken() {
        final OAuth2Token clientOAuth2Token = retrieveClientToken();

        apiClient.instantiatePisAuthFilter(clientOAuth2Token);
    }

    public void instantiateAuthFilterWithAccessToken(String authCode) {
        final OAuth2Token accessToken = retrieveAccessToken(authCode);

        apiClient.instantiatePisAuthFilter(accessToken);
    }

    private OAuth2Token retrieveClientToken() {
        final OAuth2Token clientOAuth2Token = apiClient.requestClientCredentials();

        authenticationValidator.validateClientToken(clientOAuth2Token);

        return clientOAuth2Token;
    }

    private OAuth2Token retrieveAccessToken(String authCode) {
        final OAuth2Token oAuth2Token = apiClient.exchangeAccessCode(authCode);

        authenticationValidator.validateAccessToken(oAuth2Token);

        return oAuth2Token;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.AuthTokenCategory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.UkPisAuthToken;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class UkOpenBankingPisAuthFilterInstantiator {

    private final UkOpenBankingPisAuthApiClient apiClient;
    private final OpenIdAuthenticationValidator authenticationValidator;
    private final UkOpenBankingPaymentStorage storage;

    public void instantiateAuthFilterWithClientToken() {
        if (!hasValidClientTokenInStorage()) {
            final UkPisAuthToken clientToken = retrieveClientToken();
            storage.storeToken(clientToken);
        }
    }

    public void instantiateAuthFilterWithAccessToken(String authCode) {
        if (!hasValidAccessTokenInStorage()) {
            final UkPisAuthToken accessToken = retrieveAccessToken(authCode);
            storage.storeToken(accessToken);
        }
    }

    private UkPisAuthToken retrieveClientToken() {
        final OAuth2Token clientOAuth2Token = apiClient.requestClientCredentials();

        authenticationValidator.validateClientToken(clientOAuth2Token);

        return UkPisAuthToken.builder()
                .oAuth2Token(clientOAuth2Token)
                .tokenCategory(AuthTokenCategory.CLIENT_TOKEN)
                .build();
    }

    private UkPisAuthToken retrieveAccessToken(String authCode) {
        final OAuth2Token oAuth2Token = apiClient.exchangeAccessCode(authCode);

        authenticationValidator.validateAccessToken(oAuth2Token);

        return UkPisAuthToken.builder()
                .oAuth2Token(oAuth2Token)
                .tokenCategory(AuthTokenCategory.ACCESS_TOKEN)
                .build();
    }

    private boolean hasValidClientTokenInStorage() {
        return hasValidTokenInStorage(AuthTokenCategory.CLIENT_TOKEN);
    }

    private boolean hasValidAccessTokenInStorage() {
        return hasValidTokenInStorage(AuthTokenCategory.ACCESS_TOKEN);
    }

    private boolean hasValidTokenInStorage(AuthTokenCategory expectedTokenCategory) {
        if (storage.hasToken()) {
            final UkPisAuthToken authToken = storage.getToken();

            return authToken.getTokenCategory() == expectedTokenCategory
                    && authToken.getOAuth2Token().isValid();
        }

        return false;
    }
}

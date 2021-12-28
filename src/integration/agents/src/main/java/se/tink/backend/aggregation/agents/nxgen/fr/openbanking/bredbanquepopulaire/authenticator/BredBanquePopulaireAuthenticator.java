package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class BredBanquePopulaireAuthenticator implements OAuth2Authenticator {
    private final BredBanquePopulaireApiClient bredBanquePopulaireApiClient;
    private final PersistentStorage persistentStorage;

    @Override
    public URL buildAuthorizeUrl(String state) {
        return bredBanquePopulaireApiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {
        return bredBanquePopulaireApiClient.exchangeAuthorizationToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return bredBanquePopulaireApiClient.exchangeRefreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}

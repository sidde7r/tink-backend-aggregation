package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BpceGroupAuthenticator implements OAuth2Authenticator {

    private final BpceGroupApiClient bpceGroupApiClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;

    @Override
    public URL buildAuthorizeUrl(String state) {
        return bpceGroupApiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return bpceGroupApiClient.exchangeAuthorizationToken(code).toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return bpceGroupApiClient.exchangeRefreshToken(refreshToken).toOauthToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        bpceOAuth2TokenStorage.storeToken(accessToken);
    }
}

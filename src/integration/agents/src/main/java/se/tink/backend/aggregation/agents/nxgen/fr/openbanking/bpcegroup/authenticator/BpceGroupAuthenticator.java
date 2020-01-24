package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class BpceGroupAuthenticator implements OAuth2Authenticator {

    private final BpceGroupApiClient bpceGroupApiClient;

    @Override
    public URL buildAuthorizeUrl(String state) {
        return bpceGroupApiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        final TokenResponse tokenResponse = bpceGroupApiClient.exchangeAuthorizationToken(code);

        return convertResponseToOAuthToken(tokenResponse);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final TokenResponse tokenResponse = bpceGroupApiClient.exchangeRefreshToken(refreshToken);

        return convertResponseToOAuthToken(tokenResponse);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        bpceGroupApiClient.storeAccessToken(accessToken);
    }

    private static OAuth2Token convertResponseToOAuthToken(TokenResponse response) {

        return OAuth2Token.create(
                response.getTokenType(),
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getExpiresIn());
    }
}

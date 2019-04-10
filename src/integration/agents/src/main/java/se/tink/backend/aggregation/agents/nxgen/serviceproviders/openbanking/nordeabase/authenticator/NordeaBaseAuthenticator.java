package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public abstract class NordeaBaseAuthenticator implements OAuth2Authenticator {
    protected final NordeaBaseApiClient apiClient;
    protected final SessionStorage sessionStorage;

    public NordeaBaseAuthenticator(NordeaBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public abstract URL buildAuthorizeUrl(String state);

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        GetTokenForm form =
                GetTokenForm.builder()
                        .setCode(code)
                        .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(apiClient.getConfiguration().getRedirectUrl())
                        .build();

        return apiClient.getToken(form);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        RefreshTokenForm form =
                RefreshTokenForm.builder()
                        .setRefreshToken(refreshToken)
                        .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(apiClient.getConfiguration().getRedirectUrl())
                        .build();

        OAuth2Token token = apiClient.refreshToken(form);
        apiClient.setTokenToSession(token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        apiClient.setTokenToSession(accessToken);
    }
}

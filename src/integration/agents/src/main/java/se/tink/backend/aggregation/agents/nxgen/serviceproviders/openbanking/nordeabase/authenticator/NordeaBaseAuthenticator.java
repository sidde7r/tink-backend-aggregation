package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public abstract class NordeaBaseAuthenticator implements OAuth2Authenticator {
    protected final NordeaBaseApiClient apiClient;

    public NordeaBaseAuthenticator(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public abstract URL buildAuthorizeUrl(String state);

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        GetTokenForm form =
                GetTokenForm.builder()
                        .setCode(code)
                        .setGrantType(NordeaBaseConstants.FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(apiClient.getRedirectUrl())
                        .build();

        return apiClient.getToken(form);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        OAuth2Token token = apiClient.refreshToken(refreshToken);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}

package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class DeutscheBankAuthenticator implements OAuth2Authenticator {
  private final DeutscheBankApiClient apiClient;

  public DeutscheBankAuthenticator(DeutscheBankApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public URL buildAuthorizeUrl(String state) {
    return apiClient.getAuthorizeUrl(state);
  }

  @Override
  public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
    return apiClient.getToken(code);
  }

  @Override
  public OAuth2Token refreshAccessToken(String refreshToken)
      throws BankServiceException, SessionException {
    OAuth2Token token = apiClient.refreshToken(refreshToken);
    apiClient.setTokenToSession(token);
    return token;
  }

  @Override
  public void useAccessToken(OAuth2Token accessToken) {
    apiClient.setTokenToSession(accessToken);
  }
}

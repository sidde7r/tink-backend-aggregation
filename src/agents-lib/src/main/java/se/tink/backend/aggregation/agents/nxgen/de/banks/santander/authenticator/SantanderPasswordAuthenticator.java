package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.authenticator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SantanderPasswordAuthenticator implements PasswordAuthenticator {

  private final SantanderApiClient client;
  Logger logger = LoggerFactory.getLogger(SantanderPasswordAuthenticator.class);

  public SantanderPasswordAuthenticator(SantanderApiClient client) {
    this.client = client;
  }

  @Override
  public void authenticate(String username, String password)
      throws AuthenticationException, AuthorizationException {
    LoginRequest request = new LoginRequest(username, password);
    try {
      client.login(request);
    } catch (HttpResponseException e) {
      String error = e.getResponse().getBody(String.class);
      if (StringUtils.containsIgnoreCase(error, SantanderConstants.ERROR.WRONG_PASSWORD_CODE)) {
        throw LoginError.INCORRECT_CREDENTIALS.exception();
      }
      logger.error(
          "{} Unable to authenticate error {}",
          SantanderConstants.LOGTAG.SANTANDER_LOGIN_ERROR,
          error);
      throw e;
    }
  }
}

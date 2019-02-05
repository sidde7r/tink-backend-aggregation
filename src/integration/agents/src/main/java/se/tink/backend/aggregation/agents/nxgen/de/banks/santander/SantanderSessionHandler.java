package se.tink.backend.aggregation.agents.nxgen.de.banks.santander;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SantanderSessionHandler implements SessionHandler {

  private final SantanderApiClient santanderApiClient;

  public SantanderSessionHandler(SantanderApiClient santanderApiClient) {
    this.santanderApiClient = santanderApiClient;
  }

  @Override
  public void logout() {}

  @Override
  public void keepAlive() throws SessionException {

    if (!santanderApiClient.tokenExists()) {
      throw SessionError.SESSION_EXPIRED.exception();
    }

    try {
      this.santanderApiClient.fetchAccounts();
    } catch (Exception e) {
      throw SessionError.SESSION_EXPIRED.exception();
    }
  }
}

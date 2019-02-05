package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;

public class LoginRequest {

  private final String username;
  private final String password;

  public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String toXml() {
    return String.format(SantanderConstants.SOAP.LOGIN_REQUEST, username, password);
  }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignInRequest {
  private String phone;
  private String password;

  public static SignInRequest build(String phoneNumber, String passcode) {
    return new SignInRequest().setPhone(phoneNumber).setPassword(passcode);
  }

  public SignInRequest setPhone(String phone) {
    this.phone = phone;
    return this;
  }

  public SignInRequest setPassword(String password) {
    this.password = password;
    return this;
  }
}

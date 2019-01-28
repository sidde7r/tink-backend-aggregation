package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmSignInRequest {
  private String phone;
  private String code;

  public static ConfirmSignInRequest build(String phoneNumber, String code) {
    return new ConfirmSignInRequest().setPhone(phoneNumber).setCode(code);
  }

  public ConfirmSignInRequest setPhone(String phone) {
    this.phone = phone;
    return this;
  }

  public ConfirmSignInRequest setCode(String code) {
    this.code = code;
    return this;
  }
}

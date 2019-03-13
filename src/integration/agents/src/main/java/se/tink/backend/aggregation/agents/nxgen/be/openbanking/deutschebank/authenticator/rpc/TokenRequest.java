package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class TokenRequest {
  private final String grantType;
  private final String code;
  private final String redirectUri;

  public TokenRequest(String grantType, String code, String redirectUri) {
    this.grantType = grantType;
    this.code = code;
    this.redirectUri = redirectUri;
  }

  public String toData() {
    return Form.builder()
        .put(DeutscheBankConstants.FormKeys.GRANT_TYPE, grantType)
        .put(DeutscheBankConstants.FormKeys.CODE, code)
        .put(DeutscheBankConstants.FormKeys.REDIRECT_URI, redirectUri)
        .build()
        .serialize();
  }
}

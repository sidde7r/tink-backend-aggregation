package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants;
import se.tink.backend.aggregation.nxgen.http.Form;

public class RefreshRequest {
  private final String refreshToken;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;

  public RefreshRequest(
      String refreshToken, String clientId, String clientSecret, String redirectUri) {
    this.refreshToken = refreshToken;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
  }

  public Object toData() {
    return Form.builder()
        .put(SebConstants.QueryKeys.CLIENT_ID, clientId)
        .put(SebConstants.QueryKeys.CLIENT_SECRET, clientSecret)
        .put(SebConstants.QueryKeys.REDIRECT_URI, redirectUri)
        .put(SebConstants.QueryKeys.REFRESH_TOKEN, refreshToken)
        .build()
        .serialize();
  }
}

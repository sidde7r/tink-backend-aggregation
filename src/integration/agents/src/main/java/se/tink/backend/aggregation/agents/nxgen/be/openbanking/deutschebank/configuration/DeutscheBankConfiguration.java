package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class DeutscheBankConfiguration implements ClientConfiguration {
  private String baseUrl;
  private String clientId;
  private String clientSecret;
  private String redirectUri;

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }
}

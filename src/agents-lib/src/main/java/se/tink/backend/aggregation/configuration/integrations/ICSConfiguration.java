package se.tink.backend.aggregation.configuration.integrations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ICSConfiguration {
  @JsonProperty private String clientId;
  @JsonProperty private String clientSecret;
  @JsonProperty private String redirectUri;
  @JsonProperty private String clientSSLCertificate;
  @JsonProperty private String rootCACertificate;
  @JsonProperty private String rootCAPassword;

  @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(ICSConfiguration.class);

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public String getClientSSLCertificate() {
    return clientSSLCertificate;
  }

  public String getRootCACertificate() {
    return rootCACertificate;
  }

  public String getRootCAPassword() {
    return rootCAPassword;
  }

  public boolean isValid() {

    if (!Strings.isNullOrEmpty(clientId)
        && !Strings.isNullOrEmpty(clientSecret)
        && !Strings.isNullOrEmpty(redirectUri)
        && !Strings.isNullOrEmpty(clientSSLCertificate)
        && !Strings.isNullOrEmpty(rootCACertificate)
        && !Strings.isNullOrEmpty(rootCAPassword)) {
      return true;
    } else {
      List<String> list = new ArrayList<>();

      if (Strings.isNullOrEmpty(clientId)) {
        list.add("clientId");
      }

      if (Strings.isNullOrEmpty(clientSecret)) {
        list.add("clientSecret");
      }

      if (Strings.isNullOrEmpty(redirectUri)) {
        list.add("redirectUri");
      }

      if (Strings.isNullOrEmpty(clientSSLCertificate)) {
        list.add("clientSSLCertificate");
      }

      if (Strings.isNullOrEmpty(rootCACertificate)) {
        list.add("rootCACertificate");
      }

      if (Strings.isNullOrEmpty(rootCAPassword)) {
        list.add("rootCAPassword");
      }

      logger.error(
          "{} - Missing ICS configuration: {}",
          ICSConstants.Logtag.MISSING_CONFIG,
          Arrays.toString(list.toArray()));

      return false;
    }
  }
}

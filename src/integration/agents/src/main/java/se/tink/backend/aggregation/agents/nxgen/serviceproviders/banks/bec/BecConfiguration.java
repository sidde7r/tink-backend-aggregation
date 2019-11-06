package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class BecConfiguration implements ClientConfiguration {

  private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

  @JsonProperty @Secret private String signingCertificateB64;
  @JsonProperty @Secret private String publicKeySalt;

  public String getSigningCertificate() {
    return new String(BASE64_DECODER.decode(signingCertificateB64), StandardCharsets.UTF_8);
  }

  public String getPublicKeySalt() {
    return publicKeySalt;
  }
}

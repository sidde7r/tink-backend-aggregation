package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

import java.security.PrivateKey;

@JsonObject
public class StarlingConfiguration implements ClientConfiguration {

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String keyUid;
    @JsonProperty private String signingKey;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getKeyUid() {
        return keyUid;
    }

    public PrivateKey getSigningKey() {
        return RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey));
    }
}

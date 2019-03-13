package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

import java.security.PrivateKey;

@JsonObject
public class StarlingConfiguration implements ClientConfiguration {

    @JsonProperty private ClientConfigurationEntity aisConfiguration;
    @JsonProperty private ClientConfigurationEntity pisConfiguration;
    @JsonProperty private String keyUid;
    @JsonProperty private String signingKey;

    public ClientConfigurationEntity getAisConfiguration() {
        return aisConfiguration;
    }

    public ClientConfigurationEntity getPisConfiguration() {
        return pisConfiguration;
    }

    public String getKeyUid() {
        return keyUid;
    }

    public PrivateKey getSigningKey() {
        return RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey));
    }
}

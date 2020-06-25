package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.security.PrivateKey;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class StarlingConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String aisClientId;
    @JsonProperty @Secret private String pisClientId;

    @JsonProperty @SensitiveSecret private String aisClientSecret;
    @JsonProperty @SensitiveSecret private String pisClientSecret;

    @JsonProperty @Secret private String keyUid;
    @JsonProperty @Secret private String signingKey;

    public ClientConfigurationEntity getAisConfiguration() {
        Preconditions.checkNotNull(aisClientId, "Starling AIS configuration could not load.");
        Preconditions.checkNotNull(aisClientSecret, "Starling AIS configuration could not load.");
        return new ClientConfigurationEntity(aisClientId, aisClientSecret);
    }

    public ClientConfigurationEntity getPisConfiguration() {

        Preconditions.checkNotNull(pisClientId, "Starling PIS configuration could not load.");
        Preconditions.checkNotNull(pisClientSecret, "Starling PIS configuration could not load.");
        return new ClientConfigurationEntity(pisClientId, pisClientSecret);
    }

    public String getKeyUid() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyUid), "Starling signing key UID could not load.");
        return keyUid;
    }

    public PrivateKey getSigningKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(signingKey), "Starling signing key could not load.");
        return RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey));
    }
}

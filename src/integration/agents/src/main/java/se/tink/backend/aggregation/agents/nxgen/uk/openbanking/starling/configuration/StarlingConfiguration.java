package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.security.PrivateKey;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class StarlingConfiguration implements ClientConfiguration {

    @JsonProperty private ClientConfigurationEntity aisConfiguration;
    @JsonProperty private ClientConfigurationEntity pisConfiguration;
    @JsonProperty private String keyUid;
    @JsonProperty private String signingKey;

    public ClientConfigurationEntity getAisConfiguration() {

        Preconditions.checkNotNull(aisConfiguration, "Starling AIS configuration could not load.");
        return aisConfiguration;
    }

    public ClientConfigurationEntity getPisConfiguration() {

        Preconditions.checkNotNull(pisConfiguration, "Starling PIS configuration could not load.");
        return pisConfiguration;
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

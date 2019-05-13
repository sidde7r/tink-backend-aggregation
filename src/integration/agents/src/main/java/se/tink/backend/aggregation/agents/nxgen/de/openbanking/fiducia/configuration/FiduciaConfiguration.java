package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class FiduciaConfiguration implements ClientConfiguration {

    private String keyId;
    private String keyPath;
    private String certificatePath;

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Key ID"));

        return keyId;
    }

    public String getKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyPath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Key Path"));

        return keyPath;
    }

    public String getCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificatePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate Path"));

        return certificatePath;
    }
}

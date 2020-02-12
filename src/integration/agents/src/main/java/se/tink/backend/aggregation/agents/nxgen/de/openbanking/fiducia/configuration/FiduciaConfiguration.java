package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class FiduciaConfiguration implements ClientConfiguration {

    private static final String INVALID_CONFIGURATION =
            "Invalid Configuration: %s cannot be empty or null";

    private String keyId;
    private String keyPath;
    private String certificatePath;
    private String redirectUrl;
    private String certificateId;
    private String certificate;
    private String tppId;

    public String getTppId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(tppId), String.format(INVALID_CONFIGURATION, "TPP-ID"));
        return tppId;
    }

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId), String.format(INVALID_CONFIGURATION, "Key ID"));

        return keyId;
    }

    public String getKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyPath), String.format(INVALID_CONFIGURATION, "Key Path"));

        return keyPath;
    }

    public String getCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificatePath),
                String.format(INVALID_CONFIGURATION, "Certificate Path"));

        return certificatePath;
    }

    public String getCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificate),
                String.format(INVALID_CONFIGURATION, "Certificate"));
        return certificate;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(INVALID_CONFIGURATION, "Redirect URL"));
        return redirectUrl;
    }
}

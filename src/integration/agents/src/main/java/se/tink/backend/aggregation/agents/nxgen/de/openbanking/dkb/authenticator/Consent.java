package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class Consent {
    private String consentId;
    private String consentStatus;

    boolean isValid() {
        return "valid".equalsIgnoreCase(consentStatus);
    }

    boolean isNotAuthorized() {
        return "received".equalsIgnoreCase(consentStatus);
    }

    boolean isExpired() {
        return "expired".equalsIgnoreCase(consentStatus);
    }

    boolean isRevokedByPsu() {
        return "revokedByPsu".equalsIgnoreCase(consentStatus);
    }
}

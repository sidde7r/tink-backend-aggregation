package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private String category;
    private String code;
    private String text;

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public boolean isConsentExpired() {
        return code.equalsIgnoreCase(ErrorCodes.CONSENT_EXPIRED);
    }

    public boolean isConsentInvalid() {
        return code.equalsIgnoreCase(ErrorCodes.CONSENT_INVALID);
    }

    public boolean isServiceBlocked() {
        return code.equalsIgnoreCase(ErrorCodes.SERVICE_BLOCKED);
    }
}

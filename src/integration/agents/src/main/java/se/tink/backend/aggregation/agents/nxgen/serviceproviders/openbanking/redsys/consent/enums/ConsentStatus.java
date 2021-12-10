package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums;

import java.util.Arrays;

public enum ConsentStatus {
    RECEIVED("received"),
    REJECTED("rejected"),
    PARTIALLY_AUTHORISED("partiallyAuthorised"),
    VALID("valid"),
    REVOKED_BY_PSU("revokedByPsu"),
    EXPIRED("expired"),
    TERMINATED_BY_TPP("terminatedByTpp"),
    UNKNOWN("");

    private String statusText;

    ConsentStatus(String status) {
        this.statusText = status;
    }

    public static ConsentStatus fromString(String status) {
        return Arrays.stream(ConsentStatus.values())
                .filter(s -> s.statusText.equalsIgnoreCase(status))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getStatusText() {
        return statusText;
    }
}

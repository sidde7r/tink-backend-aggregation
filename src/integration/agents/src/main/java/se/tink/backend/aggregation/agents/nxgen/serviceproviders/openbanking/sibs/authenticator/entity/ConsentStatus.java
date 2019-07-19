package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

public enum ConsentStatus {
    RCVD, // Received
    PDNG, // Pending
    PATC,
    ACTC, // Accepted
    ACFC,
    ACSP,
    ACSC,
    ACCC,
    RJCT, // Rejected
    CANC; // Canceled

    public boolean isFinalStatus() {
        return this == ACTC || this == CANC || this == RJCT;
    }
}

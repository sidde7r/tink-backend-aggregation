package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

public enum ConsentStatus {
    RCVD, // Recieved
    PDNG, // Pending
    PATC,
    ACTC, // Accepted
    ACFC,
    ACSP,
    ACSC,
    ACCC,
    RJCT, // Rejected
    CANC; // Canceled

    public boolean isFailingStatus() {
        return this == RJCT || this == CANC;
    }

    public boolean isAwaitableStatus() {
        return this == RCVD || this == PDNG;
    }

    public boolean isAcceptedStatus() {
        return this == ACTC;
    }
}

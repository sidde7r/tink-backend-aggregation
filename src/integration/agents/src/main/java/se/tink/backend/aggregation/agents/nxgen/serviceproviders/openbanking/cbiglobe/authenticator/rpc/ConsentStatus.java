package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

public enum ConsentStatus {
    RECEIVED,
    REJECTED,
    VALID,
    REVOKEDBYPSU,
    EXPIRED,
    TERMINATEDBYTPP,
    REPLACED,
    INVALIDATED,
    PENDINGEXPIRED;

    public boolean isAcceptedStatus() {
        return this == VALID;
    }
}

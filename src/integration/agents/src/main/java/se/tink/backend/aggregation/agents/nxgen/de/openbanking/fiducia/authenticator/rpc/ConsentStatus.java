package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

public enum ConsentStatus {
    RECEIVED,
    REJECTED,
    VALID,
    REVOKEDBYPSU,
    EXPIRED,
    TERMINATEDBYTPP;

    public boolean isAcceptedStatus() {
        return this == VALID;
    }
}

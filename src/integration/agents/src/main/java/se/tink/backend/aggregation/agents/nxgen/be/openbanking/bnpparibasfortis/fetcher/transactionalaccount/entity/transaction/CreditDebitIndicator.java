package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.transaction;

public enum CreditDebitIndicator {
    DBIT("DBIT"),
    CRDT("CRDT");

    private final String code;

    CreditDebitIndicator(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

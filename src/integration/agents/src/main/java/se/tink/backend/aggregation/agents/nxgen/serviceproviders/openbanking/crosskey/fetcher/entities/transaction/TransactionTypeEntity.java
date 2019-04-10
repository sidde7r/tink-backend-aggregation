package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

public enum TransactionTypeEntity {
    CREDIT("Credit"),
    DEBIT("Debit");

    private final String value;

    TransactionTypeEntity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

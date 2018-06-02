package se.tink.backend.connector.rpc;

public enum TransactionContainerType {
    REAL_TIME,
    HISTORICAL,
    BATCH;

    public static final String DOCUMENTED = "REAL_TIME, HISTORICAL, BATCH";
}

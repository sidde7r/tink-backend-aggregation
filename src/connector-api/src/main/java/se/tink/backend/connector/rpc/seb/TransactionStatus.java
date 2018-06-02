package se.tink.backend.connector.rpc.seb;

public enum TransactionStatus {

    RESERVED,
    BOOKED;

    public static final String DOCUMENTED = "RESERVED, BOOKED";
}

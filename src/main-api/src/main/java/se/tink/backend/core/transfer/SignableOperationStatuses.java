package se.tink.backend.core.transfer;

public enum SignableOperationStatuses {
    CREATED,
    EXECUTING,
    AWAITING_CREDENTIALS,
    CANCELLED,
    FAILED,
    EXECUTED;

    public static final String DOCUMENTED = "CREATED, EXECUTING, AWAITING_CREDENTIALS, CANCELLED, FAILED, EXECUTED";
}

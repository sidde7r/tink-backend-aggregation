package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

public enum CredentialsRequestType {
    REFRESH_INFORMATION,
    TRANSFER,
    KEEP_ALIVE,
    DELETE,
    CREATE,
    UPDATE,
    REENCRYPT,
    MIGRATE,
    MANUAL_AUTHENTICATION
}

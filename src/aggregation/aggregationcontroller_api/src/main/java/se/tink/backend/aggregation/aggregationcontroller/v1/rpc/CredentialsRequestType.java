package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.google.common.base.Enums;

public enum CredentialsRequestType {
    REFRESH_INFORMATION,
    TRANSFER,
    KEEP_ALIVE,
    DELETE,
    CREATE,
    UPDATE,
    REENCRYPT,
    MIGRATE,
    MANUAL_AUTHENTICATION;

    public static CredentialsRequestType translateFromServiceRequestType(
            se.tink.libraries.credentials.service.CredentialsRequestType
                    serviceCredentialsRequestType) {
        if (serviceCredentialsRequestType == null) {
            return null;
        }
        String name = serviceCredentialsRequestType.name();
        return Enums.getIfPresent(CredentialsRequestType.class, name).orNull();
    }
}

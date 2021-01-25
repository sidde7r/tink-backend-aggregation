package se.tink.backend.aggregation.events;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.eventproducerservice.events.grpc.CredentialsRefreshCommandChainFinishedProto.CredentialsRefreshCommandChainFinished.CredentialStatus;

public class CredentialsStatusConverter {
    private static final ImmutableMap<CredentialsStatus, CredentialStatus> CREDENTIALS_STATUS_MAP =
            ImmutableMap.<CredentialsStatus, CredentialStatus>builder()
                    .put(
                            CredentialsStatus.AUTHENTICATING,
                            CredentialStatus.CREDENTIAL_STATUS_AUTHENTICATING)
                    .put(
                            CredentialsStatus.AUTHENTICATION_ERROR,
                            CredentialStatus.CREDENTIAL_STATUS_AUTHENTICATION_ERROR)
                    .put(
                            CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION,
                            CredentialStatus
                                    .CREDENTIAL_STATUS_AWAITING_MOBILE_BANKID_AUTHENTICATION)
                    .put(
                            CredentialsStatus.AWAITING_OTHER_CREDENTIALS_TYPE,
                            CredentialStatus.CREDENTIAL_STATUS_AWAITING_OTHER_CREDENTIALS_TYPE)
                    .put(
                            CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION,
                            CredentialStatus.CREDENTIAL_STATUS_AWAITING_SUPPLEMENTAL_INFORMATION)
                    .put(
                            CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION,
                            CredentialStatus
                                    .CREDENTIAL_STATUS_AWAITING_THIRD_PARTY_APP_AUTHENTICATION)
                    .put(CredentialsStatus.CREATED, CredentialStatus.CREDENTIAL_STATUS_CREATED)
                    .put(CredentialsStatus.DELETED, CredentialStatus.CREDENTIAL_STATUS_DELETED)
                    .put(CredentialsStatus.DISABLED, CredentialStatus.CREDENTIAL_STATUS_DISABLED)
                    .put(CredentialsStatus.HINTED, CredentialStatus.CREDENTIAL_STATUS_HINTED)
                    .put(CredentialsStatus.METRIC, CredentialStatus.CREDENTIAL_STATUS_METRIC)
                    .put(
                            CredentialsStatus.NOT_IMPLEMENTED_ERROR,
                            CredentialStatus.CREDENTIAL_STATUS_NOT_IMPLEMENTED_ERROR)
                    .put(
                            CredentialsStatus.PERMANENT_ERROR,
                            CredentialStatus.CREDENTIAL_STATUS_PERMANENT_ERROR)
                    .put(
                            CredentialsStatus.SESSION_EXPIRED,
                            CredentialStatus.CREDENTIAL_STATUS_SESSION_EXPIRED)
                    .put(
                            CredentialsStatus.TEMPORARY_ERROR,
                            CredentialStatus.CREDENTIAL_STATUS_TEMPORARY_ERROR)
                    .put(CredentialsStatus.UNCHANGED, CredentialStatus.CREDENTIAL_STATUS_UNCHANGED)
                    .put(CredentialsStatus.UPDATED, CredentialStatus.CREDENTIAL_STATUS_UPDATED)
                    .put(CredentialsStatus.UPDATING, CredentialStatus.CREDENTIAL_STATUS_UPDATING)
                    .build();

    public static CredentialStatus convert(CredentialsStatus status) {
        return CREDENTIALS_STATUS_MAP.getOrDefault(
                status, CredentialStatus.CREDENTIAL_STATUS_UNKNOWN);
    }
}

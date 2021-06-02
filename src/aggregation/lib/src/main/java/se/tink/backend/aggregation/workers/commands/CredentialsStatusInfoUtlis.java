package se.tink.backend.aggregation.workers.commands;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;

@Slf4j
public class CredentialsStatusInfoUtlis {
    private static final String CREDENTIAL_STATUS_PAYLOAD_LOG =
            "[UPDATE CREDENTIALS] Starting update with credentials status: {}";
    private static final String CREDENTIAL_SUPPLEMENTAL_INFO_LOG =
            "[UPDATE CREDENTIALS] Starting update credential supplemental info: {}";
    private static final String CREDENTIAL_STATUS_LOG =
            "[UPDATE CREDENTIALS] Starting update with credentials status: {}";

    public static void logCredentialsInfo(Credentials credentials) {
        if (isCredentialsSupplementalInformationAvailable(credentials)) {
            log.info(CREDENTIAL_SUPPLEMENTAL_INFO_LOG, credentials.getSupplementalInformation());
        }
        if (isCredentialsStatusPayloadAvailable(credentials)) {
            log.info(CREDENTIAL_STATUS_PAYLOAD_LOG, credentials.getStatusPayload());
        }
        if (isCredentialsStatusAvailable(credentials)) {
            log.info(CREDENTIAL_STATUS_LOG, credentials.getStatus());
        }
    }

    private static boolean isCredentialsSupplementalInformationAvailable(Credentials credentials) {
        return credentials.getSupplementalInformation() != null;
    }

    private static boolean isCredentialsStatusPayloadAvailable(Credentials credentials) {
        return credentials.getStatusPayload() != null;
    }

    private static boolean isCredentialsStatusAvailable(Credentials credentials) {
        return credentials.getStatus() != null;
    }
}

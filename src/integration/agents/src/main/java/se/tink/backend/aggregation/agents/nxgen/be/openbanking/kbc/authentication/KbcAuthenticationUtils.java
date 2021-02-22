package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.errors.rpc.KbcErrorMessage;

public final class KbcAuthenticationUtils {
    public static boolean doesResponseContainCode(KbcErrorMessage errorResponse, String errorCode) {
        if (errorResponse == null) {
            return false;
        }
        return errorResponse.getTppMessages().stream().anyMatch(e -> errorCode.equals(e.getCode()));
    }
}

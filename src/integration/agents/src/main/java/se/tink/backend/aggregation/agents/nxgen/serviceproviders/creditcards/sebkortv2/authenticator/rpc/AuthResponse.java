package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthResponse {
    private String errorCode;
    private String message;
    private String returnCode;
    private String body;

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(returnCode);
    }

    public boolean isBankSideFailure() {
        return SebKortConstants.ErrorCode.GENERIC_TECHNICAL_ERROR.equalsIgnoreCase(errorCode)
                && !Strings.isNullOrEmpty(message)
                && message.toLowerCase().contains(SebKortConstants.ErrorMessage.BANK_SIDE_FAILURE);
    }
}

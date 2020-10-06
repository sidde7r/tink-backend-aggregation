package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandelsbankenErrorResponse {
    private String httpCode;
    private String httpMessage;
    private String moreInformation;

    @JsonIgnore
    public void parseAndThrow(Throwable cause) throws PaymentException {
        throw new PaymentException(
                String.format(
                        ExceptionMessages.PAYMENT_EXCEPTION,
                        httpCode,
                        httpMessage,
                        moreInformation),
                cause);
    }

    @JsonIgnore
    public boolean isTokenNotActiveError() {
        return Errors.TOKEN_NOT_ACTIVE.equalsIgnoreCase(moreInformation);
    }

    @JsonIgnore
    public boolean hasNotRegisteredToPlanError() {
        return Errors.NOT_REGISTERED_TO_PLAN.equals(moreInformation);
    }
}

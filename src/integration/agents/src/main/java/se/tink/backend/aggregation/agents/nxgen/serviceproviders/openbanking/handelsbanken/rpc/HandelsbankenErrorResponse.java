package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc;

import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
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
}

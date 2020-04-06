package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.PaymentValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private String propertyPath;
    private String errorCode;

    public String getPropertyPath() {
        return propertyPath;
    }

    /**
     * SBAB currently return error code unknown for any issue with the payment date. We've requested
     * a better description of the error.
     */
    @JsonIgnore
    public boolean isInvalidDate() {
        return PaymentValue.TRANSACTION_DATE.equalsIgnoreCase(propertyPath)
                && PaymentValue.UNKNOWN.equalsIgnoreCase(errorCode);
    }

    @JsonIgnore
    public boolean isFailedSignature() {
        return Errors.SIGNATURE_FAILED.equalsIgnoreCase(errorCode);
    }

    @JsonIgnore
    public boolean isAmountLimitReached() {
        return PaymentValue.AMOUNT.equalsIgnoreCase(propertyPath)
                && Errors.AMOUNT_LIMIT_REACHED.equalsIgnoreCase(errorCode);
    }

    public boolean isAmountExceedsCurrentBalance() {
        return PaymentValue.AMOUNT.equalsIgnoreCase(propertyPath)
                && Errors.AMOUNT_EXCEEDS_BALANCE_VALIDATION.equalsIgnoreCase(errorCode);
    }
}

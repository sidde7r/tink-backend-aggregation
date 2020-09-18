package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private int errorCode;
    private String errorMessage;
    private String propertyPath;

    public boolean isInvalidAccountNumber() {
        return SBABConstants.ErrorCode.INVALID_ACCOUNT_NUMBER == errorCode;
    }

    public boolean isInvalidAccountNumberLength() {
        return SBABConstants.ErrorCode.INVALID_ACCOUNT_NUMBER_LEN == errorCode;
    }

    public boolean isInvalidTransactionDate() {
        return SBABConstants.ErrorCode.INVALID_TRANSACTION_DATE == errorCode;
    }

    public boolean isExcessTransferAmount() {
        return SBABConstants.ErrorCode.EXCESS_TRANSFER_AMOUNT == errorCode;
    }
}

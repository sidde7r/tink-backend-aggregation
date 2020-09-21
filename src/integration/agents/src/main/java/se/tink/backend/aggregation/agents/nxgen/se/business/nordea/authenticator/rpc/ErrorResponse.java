package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class ErrorResponse {
    @JsonProperty("http_status")
    private int httpStatus;

    @JsonProperty private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty private List<Detail> details;

    /**
     * @throws HttpResponseException if the http response doesn't have application/json content type
     *     header
     */
    public static ErrorResponse of(HttpResponseException e) throws HttpResponseException {
        HttpResponse response = e.getResponse();
        if (Objects.nonNull(response)
                && MediaType.APPLICATION_JSON_TYPE.equals(response.getType())) {
            return response.getBody(ErrorResponse.class);
        }
        throw e;
    }

    @JsonIgnore
    public boolean isInvalidAccessToken() {
        return ErrorCodes.INVALID_TOKEN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean tokenRequired() {
        return ErrorCodes.TOKEN_REQUIRED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isResourceNotFound() {
        return ErrorCodes.RESOURCE_NOT_FOUND.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isInvalidRefreshToken() {
        return ErrorCodes.INVALID_GRANT.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoAgreement() {
        return ErrorCodes.AGREEMENT_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoClassification() {
        return ErrorCodes.CLASSIFICATION_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoConnectedAccount() {
        return ErrorCodes.UNABLE_TO_LOAD_CUSTOMER.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isDuplicatePayment() {
        return ErrorCodes.DUPLICATE_PAYMENT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isUnregisteredRecipient() {
        return ErrorCodes.UNREGISTERED_RECIPIENT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isNotEnoughFunds() {
        return ErrorCodes.NOT_ENOUGH_FUNDS.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isExternalServiceCallFailed() {
        return ErrorCodes.EXTERNAL_SERVICE_CALL_FAILED.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isSigningCollision() {
        return ErrorCodes.SIGNING_COLLISION.equalsIgnoreCase(error)
                && ErrorCodes.SIGNING_COLLISION_MESSAGE.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isWrongToAccountLengthError() {
        return ErrorCodes.WRONG_TO_ACCOUNT_LENGTH.equalsIgnoreCase(error)
                && ErrorCodes.WRONG_TO_ACCOUNT_LENGHT_MESSAGE.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isInvalidOcr() {
        return HttpStatus.SC_BAD_REQUEST == httpStatus
                && ErrorCodes.INVALID_OCR_ERROR_CODE.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isUserUnauthorizedError() {
        return ErrorCodes.USER_UNAUTHORIZED.equalsIgnoreCase(error)
                && ErrorCodes.USER_UNAUTHORIZED_MESSAGE.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isBankServiceError() {
        return HttpStatus.SC_INTERNAL_SERVER_ERROR == httpStatus
                && (isHysterixShortCircuited() || isUnexpectedError());
    }

    private boolean isUnexpectedError() {
        return errorDescription.toLowerCase().contains(ErrorCodes.UNEXPECTED_EXECUTION_ERROR)
                && ErrorCodes.UNEXPECTED_EXECUTION_ERROR_CODE.equalsIgnoreCase(error);
    }

    private boolean isHysterixShortCircuited() {
        return errorDescription.toLowerCase().contains(ErrorCodes.HYSTRIX_CIRCUIT_SHORT_CIRCUITED)
                && ErrorCodes.ERROR_CORE_UNKNOWN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isTimeoutError() {
        return errorDescription.toLowerCase().contains(ErrorCodes.TIMEOUT_AFTER_MESSAGE)
                && ErrorCodes.ERROR_CORE_UNKNOWN.equalsIgnoreCase(error)
                && HttpStatus.SC_INTERNAL_SERVER_ERROR == httpStatus;
    }

    @JsonIgnore
    public boolean isInvalidPaymentMessage() {
        return HttpStatus.SC_BAD_REQUEST == httpStatus
                && ErrorCodes.INVALID_PARAMETERS_FOR_PAYMENT.equalsIgnoreCase(errorDescription)
                && ErrorCodes.BESE1076.equalsIgnoreCase(error)
                && buildErrorMessage().contains(ErrorCodes.OWN_MESSAGE_CONSTRAINTS);
    }

    @JsonIgnore
    public boolean needsToRefreshToken() {
        return tokenRequired() || isInvalidAccessToken();
    }

    @JsonIgnore
    public void throwAppropriateErrorIfAny() {
        if (isTimeoutError()) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(errorDescription);
        }
        if (isBankServiceError()) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(errorDescription);
        }
        if (isInvalidPaymentMessage()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(EndUserMessage.INVALID_MESSAGE)
                    .setMessage(buildErrorMessage())
                    .build();
        }
        if (isExternalServiceCallFailed()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(NordeaSEConstants.LogMessages.BANKSIDE_ERROR_WHEN_SEARCHING_OUTBOX)
                    .setEndUserMessage(EndUserMessage.TRANSFER_EXECUTE_FAILED)
                    .build();
        }
        if (isDuplicatePayment()) {
            throw duplicatePaymentError();
        }
        if (isWrongToAccountLengthError()) {
            throw wrongToAccountLengthError();
        }
        if (isUnregisteredRecipient()) {
            throw transferRejectedError(
                    ErrorCodes.UNREGISTERED_RECIPIENT, EndUserMessage.UNREGISTERED_RECIPIENT);
        }
        if (isNotEnoughFunds()) {
            throw notEnoughFundsError();
        }
        if (isSigningCollision()) {
            throw bankIdAlreadyInProgressError(null);
        }
        if (isWrongToAccountLengthError()) {
            throw wrongToAccountLengthError();
        }
        if (isInvalidOcr()) {
            throw wrongOcrError();
        }
        if (isUserUnauthorizedError()) {
            throw userUnauthorizedError();
        }
    }

    @JsonIgnore
    private String buildErrorMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(errorDescription);
        if (!details.isEmpty()) {
            sb.append(": ");
            for (Detail detail : details) {
                sb.append(detail.getMoreInfo()).append(", ").append(detail.getParam()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static TransferExecutionException invalidDestError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                .build();
    }

    public static TransferExecutionException failedFetchAccountsError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(ErrorCodes.UNABLE_TO_FETCH_ACCOUNTS)
                .build();
    }

    public static TransferExecutionException paymentFailedError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(ErrorCodes.PAYMENT_ERROR)
                .setEndUserMessage(ErrorCodes.PAYMENT_ERROR)
                .setException(e)
                .build();
    }

    public static TransferExecutionException invalidSourceAccountError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(EndUserMessage.INVALID_SOURCE)
                .build();
    }

    public static TransferExecutionException invalidPaymentType() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage("You can only make payments to Swedish destinations")
                .build();
    }

    public static TransferExecutionException bankIdAlreadyInProgressError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_ANOTHER_IN_PROGRESS.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_ANOTHER_IN_PROGRESS)
                .setException(e)
                .build();
    }

    public static TransferExecutionException bankIdCancelledError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_CANCELLED)
                .build();
    }

    public static TransferExecutionException bankIdTimedOut() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_NO_RESPONSE.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_NO_RESPONSE)
                .build();
    }

    protected TransferExecutionException notEnoughFundsError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.EXCESS_AMOUNT.getKey().get())
                .setEndUserMessage(EndUserMessage.EXCESS_AMOUNT)
                .build();
    }

    public static TransferExecutionException signTransferFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(EndUserMessage.BANKID_TRANSFER_FAILED.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_TRANSFER_FAILED)
                .build();
    }

    protected TransferExecutionException duplicatePaymentError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(EndUserMessage.DUPLICATE_PAYMENT)
                .setMessage(EndUserMessage.DUPLICATE_PAYMENT.getKey().get())
                .setInternalStatus(InternalStatus.DUPLICATE_PAYMENT.toString())
                .build();
    }

    public static TransferExecutionException transferRejectedError(
            String errorMessage, EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(errorMessage)
                .setEndUserMessage(endUserMessage)
                .build();
    }

    public static TransferExecutionException transferFailedError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(EndUserMessage.TRANSFER_EXECUTE_FAILED)
                .setException(e)
                .build();
    }

    public TransferExecutionException wrongToAccountLengthError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(NordeaSEConstants.LogMessages.WRONG_TO_ACCOUNT_LENGTH)
                .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                .build();
    }

    private TransferExecutionException wrongOcrError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(NordeaSEConstants.LogMessages.WRONG_OCR_MESSAGE)
                .setEndUserMessage(EndUserMessage.INVALID_OCR)
                .build();
    }

    public TransferExecutionException userUnauthorizedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(NordeaSEConstants.LogMessages.USER_UNAUTHORIZED_MESSAGE)
                .setEndUserMessage(EndUserMessage.USER_UNAUTHORIZED)
                .build();
    }
}

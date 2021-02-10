package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ErrorResponse {
    private int httpStatus;
    private String error;
    private String errorDescription;
    private List<Detail> details;

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
    public boolean isNotACustomer() {
        return NordeaBaseConstants.ErrorCodes.RESOURCE_NOT_FOUND.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isAutostartTokenExpired() {
        return NordeaBaseConstants.ErrorCodes.AUTH_NOT_STARTED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isBankIdTimeout() {
        return NordeaBaseConstants.ErrorCodes.CHALLENGE_EXPIRED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isInvalidAccessToken() {
        return NordeaBaseConstants.ErrorCodes.INVALID_TOKEN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean tokenRequired() {
        return NordeaBaseConstants.ErrorCodes.TOKEN_REQUIRED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isInvalidRefreshToken() {
        return NordeaBaseConstants.ErrorCodes.INVALID_GRANT.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoAgreement() {
        return NordeaBaseConstants.ErrorCodes.AGREEMENT_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoClassification() {
        return NordeaBaseConstants.ErrorCodes.CLASSIFICATION_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoConnectedAccount() {
        return NordeaBaseConstants.ErrorCodes.UNABLE_TO_LOAD_CUSTOMER.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isDuplicatePayment() {
        return NordeaBaseConstants.ErrorCodes.DUPLICATE_PAYMENT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isUnregisteredRecipient() {
        return ErrorCodes.UNREGISTERED_RECIPIENT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isNotEnoughFunds() {
        return NordeaBaseConstants.ErrorCodes.NOT_ENOUGH_FUNDS.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isExternalServiceCallFailed() {
        return NordeaBaseConstants.ErrorCodes.EXTERNAL_SERVICE_CALL_FAILED.equalsIgnoreCase(
                errorDescription);
    }

    @JsonIgnore
    public boolean isSigningCollision() {
        return NordeaBaseConstants.ErrorCodes.SIGNING_COLLISION.equalsIgnoreCase(error)
                && NordeaBaseConstants.ErrorCodes.SIGNING_COLLISION_MESSAGE.equalsIgnoreCase(
                        errorDescription);
    }

    @JsonIgnore
    public boolean isWrongToAccountLengthError() {
        return NordeaBaseConstants.ErrorCodes.WRONG_TO_ACCOUNT_LENGTH.equalsIgnoreCase(error)
                && NordeaBaseConstants.ErrorCodes.WRONG_TO_ACCOUNT_LENGHT_MESSAGE.equalsIgnoreCase(
                        errorDescription);
    }

    @JsonIgnore
    public boolean isInvalidOcr() {
        return HttpStatus.SC_BAD_REQUEST == httpStatus
                && ErrorCodes.INVALID_OCR_ERROR_CODE.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isUserUnauthorizedError() {
        return NordeaBaseConstants.ErrorCodes.USER_UNAUTHORIZED.equalsIgnoreCase(error)
                && NordeaBaseConstants.ErrorCodes.USER_UNAUTHORIZED_MESSAGE.equalsIgnoreCase(
                        errorDescription);
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
                && NordeaBaseConstants.ErrorCodes.ERROR_CORE_UNKNOWN.equalsIgnoreCase(error)
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
    public boolean isInvalidBankgiroAccount() {
        return HttpStatus.SC_BAD_REQUEST == httpStatus
                && ErrorCodes.INVALID_BANKGIRO_ACCOUNT.equalsIgnoreCase(error)
                && ErrorMessages.INVALID_BANKGIRO_ACCOUNT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isPaymentNotFoundInOutbox() {
        return HttpStatus.SC_NOT_FOUND == httpStatus
                && ErrorCodes.NOT_FOUND.equalsIgnoreCase(error)
                && ErrorMessages.PAYMENT_NOT_FOUND.equalsIgnoreCase(errorDescription);
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
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            NordeaBaseConstants.LogMessages.BANKSIDE_ERROR_WHEN_SEARCHING_OUTBOX)
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
            throw unregisteredRecipientError();
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

        if (isInvalidBankgiroAccount()) {
            throw invalidDestError();
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
                .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .build();
    }

    public static TransferExecutionException failedFetchAccountsError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaBaseConstants.ErrorCodes.UNABLE_TO_FETCH_ACCOUNTS)
                .build();
    }

    public static TransferExecutionException paymentFailedError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaBaseConstants.ErrorCodes.PAYMENT_ERROR)
                .setEndUserMessage(NordeaBaseConstants.ErrorCodes.PAYMENT_ERROR)
                .setException(e)
                .build();
    }

    public static TransferExecutionException invalidSourceAccountError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString())
                .build();
    }

    public static TransferExecutionException invalidPaymentType() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage("You can only make payments to Swedish destinations")
                .setInternalStatus(InternalStatus.INVALID_PAYMENT_TYPE.toString())
                .build();
    }

    public static TransferExecutionException bankIdAlreadyInProgressError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS
                                .getKey()
                                .get())
                .setEndUserMessage(
                        TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS)
                .setInternalStatus(InternalStatus.BANKID_ANOTHER_IN_PROGRESS.toString())
                .setException(e)
                .build();
    }

    public static TransferExecutionException bankIdCancelledError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_CANCELLED)
                .setInternalStatus(InternalStatus.BANKID_CANCELLED.toString())
                .build();
    }

    public static TransferExecutionException bankIdTimedOut() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.BANKID_NO_RESPONSE.getKey().get())
                .setEndUserMessage(EndUserMessage.BANKID_NO_RESPONSE)
                .setInternalStatus(InternalStatus.BANKID_NO_RESPONSE.toString())
                .build();
    }

    protected TransferExecutionException notEnoughFundsError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT.getKey().get())
                .setEndUserMessage(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT)
                .setInternalStatus(InternalStatus.INSUFFICIENT_FUNDS.toString())
                .build();
    }

    public static TransferExecutionException signTransferFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED
                                .getKey()
                                .get())
                .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED)
                .build();
    }

    protected TransferExecutionException duplicatePaymentError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.DUPLICATE_PAYMENT)
                .setMessage(
                        TransferExecutionException.EndUserMessage.DUPLICATE_PAYMENT.getKey().get())
                .setInternalStatus(InternalStatus.DUPLICATE_PAYMENT.toString())
                .build();
    }

    public static TransferExecutionException transferRejectedError(
            String errorMessage, TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(errorMessage)
                .setEndUserMessage(endUserMessage)
                .build();
    }

    public static TransferExecutionException unregisteredRecipientError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(ErrorCodes.UNREGISTERED_RECIPIENT)
                .setEndUserMessage(EndUserMessage.UNREGISTERED_RECIPIENT)
                .setInternalStatus(InternalStatus.UNREGISTERED_RECIPIENT.toString())
                .build();
    }

    public static TransferExecutionException transferFailedError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                .setException(e)
                .build();
    }

    public TransferExecutionException wrongToAccountLengthError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(NordeaBaseConstants.LogMessages.WRONG_TO_ACCOUNT_LENGTH)
                .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .build();
    }

    private TransferExecutionException wrongOcrError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(NordeaBaseConstants.LogMessages.WRONG_OCR_MESSAGE)
                .setEndUserMessage(EndUserMessage.INVALID_OCR)
                .setInternalStatus(InternalStatus.INVALID_OCR.toString())
                .build();
    }

    public TransferExecutionException userUnauthorizedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(NordeaBaseConstants.LogMessages.USER_UNAUTHORIZED_MESSAGE)
                .setEndUserMessage(EndUserMessage.USER_UNAUTHORIZED)
                .setInternalStatus(InternalStatus.USER_UNAUTHORIZED.toString())
                .build();
    }
}

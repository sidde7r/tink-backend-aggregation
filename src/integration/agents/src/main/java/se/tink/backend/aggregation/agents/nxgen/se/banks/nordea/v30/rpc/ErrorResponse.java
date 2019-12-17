package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
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
        return NordeaSEConstants.ErrorCodes.INVALID_TOKEN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean tokenRequired() {
        return NordeaSEConstants.ErrorCodes.TOKEN_REQUIRED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isInvalidRefreshToken() {
        return NordeaSEConstants.ErrorCodes.INVALID_GRANT.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoAgreement() {
        return NordeaSEConstants.ErrorCodes.AGREEMENT_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoClassification() {
        return NordeaSEConstants.ErrorCodes.CLASSIFICATION_NOT_CONFIRMED.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean hasNoConnectedAccount() {
        return NordeaSEConstants.ErrorCodes.UNABLE_TO_LOAD_CUSTOMER.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isDuplicatePayment() {
        return NordeaSEConstants.ErrorCodes.DUPLICATE_PAYMENT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isUnregisteredRecipient() {
        return ErrorCodes.UNREGISTERED_RECIPIENT.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isNotEnoughFunds() {
        return NordeaSEConstants.ErrorCodes.NOT_ENOUGH_FUNDS.equalsIgnoreCase(errorDescription);
    }

    @JsonIgnore
    public boolean isExternalServiceCallFailed() {
        return NordeaSEConstants.ErrorCodes.EXTERNAL_SERVICE_CALL_FAILED.equalsIgnoreCase(
                errorDescription);
    }

    @JsonIgnore
    public boolean isSigningCollision() {
        return NordeaSEConstants.ErrorCodes.SIGNING_COLLISION.equalsIgnoreCase(error)
                && NordeaSEConstants.ErrorCodes.SIGNING_COLLISION_MESSAGE.equalsIgnoreCase(
                        errorDescription);
    }

    @JsonIgnore
    public boolean isWrongToAccountLengthError() {
        return NordeaSEConstants.ErrorCodes.WRONG_TO_ACCOUNT_LENGTH.equalsIgnoreCase(error)
                && NordeaSEConstants.ErrorCodes.WRONG_TO_ACCOUNT_LENGHT_MESSAGE.equalsIgnoreCase(
                        errorDescription);
    }

    @JsonIgnore
    public boolean isBankServiceError() {
        return errorDescription.toLowerCase().contains(ErrorCodes.HYSTRIX_CIRCUIT_SHORT_CIRCUITED)
                && HttpStatus.SC_INTERNAL_SERVER_ERROR == httpStatus
                && ErrorCodes.ERROR_CORE_UNKNOWN.equalsIgnoreCase(error);
    }

    @JsonIgnore
    public boolean isTimeoutError() {
        return errorDescription.toLowerCase().contains(ErrorCodes.TIMEOUT_AFTER_MESSAGE)
                && NordeaSEConstants.ErrorCodes.ERROR_CORE_UNKNOWN.equalsIgnoreCase(error)
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
}

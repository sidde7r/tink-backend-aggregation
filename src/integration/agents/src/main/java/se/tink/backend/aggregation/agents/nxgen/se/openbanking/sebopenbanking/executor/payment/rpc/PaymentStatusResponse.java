package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.SebPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentStatusResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String scaStatus;
    private String psuMessage;
    private String transactionStatus;
    private String paymentId;
    private List<String> tppMessages = new ArrayList<>();

    public String getTransactionStatus() {
        return transactionStatus;
    }

    @JsonIgnore
    public boolean hasMethodSelectionEntity() {
        return links != null && links.hasMethodSelectionEntity();
    }

    @JsonIgnore
    public boolean isReadyForSigning() {
        return hasMethodSelectionEntity()
                && SebPaymentStatus.RCVD.getText().equalsIgnoreCase(transactionStatus);
    }

    @JsonIgnore
    public boolean isPaymentCancelled() {
        return hasMethodSelectionEntity()
                && SebPaymentStatus.RCVD.getText().equalsIgnoreCase(transactionStatus);
    }

    @JsonIgnore
    public PaymentStatusResponse checkForErrors() throws PaymentException {
        if (SebPaymentStatus.RJCT.getText().equalsIgnoreCase(transactionStatus)) {
            if (isDueDateTooCloseError()) {
                throw DateValidationException.paymentDateTooCloseException();
            } else if (isDueDateNotBusinessDayError()) {
                throw DateValidationException.paymentDateNotBusinessDayException();
            } else if (isSimilarPaymentError()) {
                throw new DuplicatePaymentException();
            } else if (isServiceUnavailableError()) {
                throw PaymentRejectedException.bankPaymentServiceUnavailable();
            } else {
                throw new PaymentRejectedException(getErrorMessage());
            }
        }
        return this;
    }

    @JsonIgnore
    private boolean isDueDateTooCloseError() {
        return tppMessages.stream()
                .anyMatch(
                        tppMessage ->
                                tppMessage.contains(ErrorMessages.DATE_TOO_CLOSE_ERROR_MESSAGE));
    }

    @JsonIgnore
    private boolean isDueDateNotBusinessDayError() {
        return tppMessages.stream()
                .anyMatch(
                        tppMessage ->
                                tppMessage.contains(ErrorMessages.NOT_BUSINESS_DAY_ERROR_MESSAGE));
    }

    @JsonIgnore
    private boolean isSimilarPaymentError() {
        return tppMessages.stream()
                .anyMatch(
                        tppMessage ->
                                tppMessage.contains(ErrorMessages.SIMILAR_PAYMENT_ERROR_MESSAGE));
    }

    @JsonIgnore
    private boolean isServiceUnavailableError() {
        return tppMessages.stream()
                .anyMatch(
                        tppMessage ->
                                tppMessage.contains(ErrorMessages.PAYMENT_SERVICE_UNAVAILABLE));
    }

    @JsonIgnore
    private String getErrorMessage() {
        return tppMessages.stream().findFirst().orElse(PaymentRejectedException.MESSAGE);
    }
}

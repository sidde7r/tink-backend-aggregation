package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.core.Amount;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferRequest {
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    private String amount;
    private String annotation; // own message
    private String fromAccountNumber;
    private String message; // recipient message
    private String toAccountNumber;
    private String toClearingNo;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public String getToClearingNo() {
        return toClearingNo;
    }

    public void setToClearingNo(String toClearingNo) {
        this.toClearingNo = toClearingNo;
    }

    public static TransferRequest create(AccountEntity source, AccountEntity destination, Amount amount,
            TransferMessageFormatter.Messages formattedTransferMessages) {
        TransferRequest request = createWithoutDestination(source, amount, formattedTransferMessages);

        if (destination.getClearingNumber() == null) {
            request.setToAccountNumber(destination.getNumber());
        } else {
            request.setToAccountNumber(destination.getClearingNumber() + destination.getNumber());
        }

        return request;
    }

    public static TransferRequest create(AccountEntity source, ValidRecipientEntity destination, Amount amount,
            TransferMessageFormatter.Messages formattedTransferMessages) {
        TransferRequest request = createWithoutDestination(source, amount, formattedTransferMessages);
        request.setToAccountNumber(destination.getAccountNumber());
        request.setToClearingNo(destination.getClearingNo());

        return request;
    }

    private static TransferRequest createWithoutDestination(AccountEntity source, Amount amount,
            TransferMessageFormatter.Messages formattedTransferMessages) {
        TransferRequest request = new TransferRequest();

        request.setFromAccountNumber(source.getNumber());
        request.setAmount(AMOUNT_FORMAT.format(amount.getValue()));
        request.setAnnotation(formattedTransferMessages.getSourceMessage());
        request.setMessage(formattedTransferMessages.getDestinationMessage());

        return request;
    }
}

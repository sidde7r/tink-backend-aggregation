package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public static PaymentRequest create(Transfer transfer, AccountEntity source, RecipientAccountEntity destination) {
        PaymentRequest request = new PaymentRequest();

        request.setAccountNumber(source.getNumber());
        request.setAmount(AMOUNT_FORMAT.format(transfer.getAmount().getValue()));
        request.setMessage(transfer.getDestinationMessage());
        request.setRecipientId(destination.getId());
        request.setRecipientName(destination.getName());
        request.setRecipientType(String.valueOf(destination.getType()));
        
        if (transfer.getDueDate() != null) {            
            request.setPayDate(ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate()));
        } else {
            request.setPayDate(ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils.getNextBusinessDay(new Date())));
        }

        return request;
    }

    private String accountNumber;
    private String amount;
    private String message;
    private String payDate;
    private String recipientId;
    private String recipientName;
    private String recipientType;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public String getPayDate() {
        return payDate;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }
}

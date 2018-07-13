package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePaymentRequest {
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public static UpdatePaymentRequest create(Transfer transfer, AccountEntity source) {
        UpdatePaymentRequest request = new UpdatePaymentRequest();

        request.setAccountNumber(source.getNumber());
        request.setAmount(AMOUNT_FORMAT.format(transfer.getAmount().getValue()));
        request.setMessage(transfer.getDestinationMessage());
        request.setPayDate(ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate()));

        return request;
    }

    private String accountNumber;
    private String amount;
    private String message;
    private String payDate;

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

}

package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferRequest {
    @JsonIgnore
    private static final String TODAY_AS_TRANSFER_DATE = "Now";
    @JsonProperty("creditAccountNoExt")
    private final String destinationAccountNumber;
    @JsonProperty("debitAccount")
    private final String sourceAccountNumber;
    @JsonProperty("creditorStatementText")
    private final String destinationMessage;
    @JsonProperty("debitorStatementText")
    private final String sourceMessage;
    private final String amount;
    private final String currency;
    @JsonProperty("simpleDate")
    private final String date;
    @JsonProperty("creditAccountRegNoExt")
    private static final String CREDIT_ACCOUNT_REG_NO_EXT = "";
    @JsonProperty("sendMessageToReceiver")
    private static final String SEND_MESSAGE_TO_RECEIVER = "false";
    @JsonProperty("sendReceiptToSender")
    private static final String SEND_RECEIPT_TO_SENDER = "false";
    @JsonProperty("savePayee")
    private static final String SAVE_PAYEE = "false";
    @JsonProperty("saveForLaterApproval")
    private static final String SAVE_FOR_LATER_APPROVAL = "false";

    private TransferRequest(String amount, String currency, String date, String destinationAccountNumber,
            String destinationMessage, String sourceAccountNumber,
            String sourceMessage) {
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.destinationAccountNumber = destinationAccountNumber;
        this.destinationMessage = destinationMessage;
        this.sourceAccountNumber = sourceAccountNumber;
        this.sourceMessage = sourceMessage;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDate() {
        return date;
    }

    public static String getCreditAccountRegNoExt() {
        return CREDIT_ACCOUNT_REG_NO_EXT;
    }

    public static String getSendMessageToReceiver() {
        return SEND_MESSAGE_TO_RECEIVER;
    }

    public static String getSendReceiptToSender() {
        return SEND_RECEIPT_TO_SENDER;
    }

    public static String getSavePayee() {
        return SAVE_PAYEE;
    }

    public static String getSaveForLaterApproval() {
        return SAVE_FOR_LATER_APPROVAL;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String amount;
        private String currency;
        private String date;
        private String destinationAccountNumber;
        private String destinationMessage;
        private String sourceAccountNumber;
        private String sourceMessage;

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date.isEqual(LocalDate.now()) ?
                    TODAY_AS_TRANSFER_DATE : date.format(DateTimeFormatter.BASIC_ISO_DATE);
            return this;
        }

        public Builder destinationAccountNumber(String destinationAccountNumber) {
            this.destinationAccountNumber = destinationAccountNumber;
            return this;
        }

        public Builder destinationMessage(String destinationMessage) {
            this.destinationMessage = destinationMessage;
            return this;
        }

        public Builder sourceAccountNumber(String sourceAccountNumber) {
            this.sourceAccountNumber = sourceAccountNumber;
            return this;
        }

        public Builder sourceMessage(String sourceMessage) {
            this.sourceMessage = sourceMessage;
            return this;
        }

        public TransferRequest build() {
            Preconditions.checkNotNull(amount);
            Preconditions.checkNotNull(currency);
            Preconditions.checkNotNull(date);
            Preconditions.checkNotNull(destinationAccountNumber);
            Preconditions.checkNotNull(destinationMessage);
            Preconditions.checkNotNull(sourceAccountNumber);
            Preconditions.checkNotNull(sourceMessage);

            return new TransferRequest(amount, currency, date, destinationAccountNumber,
                    destinationMessage, sourceAccountNumber, sourceMessage);
        }
    }
}

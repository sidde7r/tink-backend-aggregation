package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.transfer.enums.TransferType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpcomingTransactionEntity extends TransactionEntity {
    private static final String FOUR_POINT_PRECISION_FORMAT_STRING = "0.0000";

    @JsonProperty("FromAccountId")
    private String accountId;
    @JsonProperty("RecipientName")
    private String recipientName;
    @JsonProperty("DueDate")
    private String dueDate;
    @JsonProperty("RecipientAccountNumber")
    private String destinationAccountNumber;
    @JsonProperty("RecipientType")
    private String destinationType;
    @JsonProperty("Memo")
    private String memo;
    @JsonProperty("ReferenceType")
    private String refrenceType;
    @JsonProperty("Reference")
    private String transferMessage;
    @JsonProperty("EventId")
    private String eventId;
    @JsonProperty("FromAccountNumber")
    private String sourceAccountNumber;

    @Override
    public void setAmount(double amount) {
        this.amount = -amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String getDescription() {
        return getRecipientName();
    }

    @Override
    public String getDate() {
        return getDueDate();
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getRefrenceType() {
        return refrenceType;
    }

    public void setRefrenceType(String refrenceType) {
        this.refrenceType = refrenceType;
    }

    public String getTransferMessage() {
        return transferMessage;
    }

    public void setTransferMessage(String transferMessage) {
        this.transferMessage = transferMessage;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getHash(boolean ignoreSource) {
        AccountIdentifier source = getSource();
        AccountIdentifier destination = getDestination();

        return String.valueOf(java.util.Objects.hash(
                getTypeForHash(type).name(),
                getAmountForHash(-amount),
                destination != null ? destination.toURIWithoutName() : null,
                transferMessage,
                // if ignoreSource is true, set source to null, otherwise use it if it exists
                (ignoreSource || source == null) ? null : source.toURIWithoutName(),
                memo,
                dueDate));
    }

    private static TransferType getTypeForHash(String type) {
        switch (type.toLowerCase()) {
        case "payment":
        case "paymentpg":
        case "paymentbg":
            return TransferType.PAYMENT;
        default:
            return TransferType.BANK_TRANSFER;
        }
    }

    private static String getAmountForHash(Double amount) {
        if (amount == null) {
            return null;
        }

        return new DecimalFormat(
                FOUR_POINT_PRECISION_FORMAT_STRING, DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                .format(amount);
    }

    public AccountIdentifier getSource() {
        if (Strings.isNullOrEmpty(sourceAccountNumber)) {
            return null;
        }

        SwedishIdentifier sourceAccount = new SwedishIdentifier(sourceAccountNumber);
        return AccountIdentifier.create(URI.create(sourceAccount.toUriAsString()));
    }

    public AccountIdentifier getDestination() {
        if (Strings.isNullOrEmpty(destinationAccountNumber)) {
            return null;
        }

        BankGiroIdentifier bankGiroIdentifier = new BankGiroIdentifier(destinationAccountNumber);
        return AccountIdentifier.create(URI.create(bankGiroIdentifier.toUriAsString()));
    }
}

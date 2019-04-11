package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.net.URI;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;

@JsonObject
public class UpcomingTransactionEntity {
    @JsonProperty("IsRejected")
    private boolean isRejected;

    @JsonProperty("EventId")
    private String eventId;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("FromAccountId")
    private String fromAccountId;

    @JsonProperty("FromAccountName")
    private String fromAccountName;

    @JsonProperty("FromAccountNumber")
    private String fromAccountNumber;

    @JsonProperty("RecipientId")
    private String recipientId;

    @JsonProperty("RecipientName")
    private String recipientName;

    @JsonProperty("RecipientAccountNumber")
    private String recipientAccountNumber;

    @JsonProperty("RecipientType")
    private String recipientType;

    @JsonDouble
    @JsonProperty("Amount")
    private double amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("DueDate")
    private Date dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("DrawDate")
    private Date drawDate;

    @JsonProperty("Memo")
    private String memo;

    @JsonProperty("ReferenceType")
    private String referenceType;

    @JsonProperty("Reference")
    private String reference;

    @JsonProperty("IsStandingTransaction")
    private boolean isStandingTransaction;

    @JsonProperty("EgiroHasLink")
    private boolean egiroHasLink;

    @JsonProperty("IsUpdateable")
    private boolean isUpdateable;

    @JsonProperty("FormattedRate")
    private String formattedRate;

    @JsonProperty("Rate")
    private double rate;

    @JsonProperty("AmountInGivenCurrency")
    private double amountInGivenCurrency;

    @JsonProperty("ApproximateAmount")
    private double approximateAmount;

    @JsonProperty("TransferCost")
    private double transferCost;

    @JsonProperty("AmountWithoutTransferCost")
    private double amountWithoutTransferCost;

    @JsonProperty("IsProcessing")
    private boolean isProcessing;

    @JsonProperty("RejectedIsViewed")
    private boolean rejectedIsViewed;

    @JsonIgnore
    public UpcomingTransaction toUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setDescription(recipientName)
                .setDate(dueDate)
                .setAmount(Amount.inSEK(-1.0 * amount))
                .build();
    }

    @JsonIgnore
    public String getHash(boolean ignoreSource) {
        AccountIdentifier source = getSource();
        AccountIdentifier destination = getDestination();

        return String.valueOf(
                java.util.Objects.hash(
                        getTypeForHash().name(),
                        amount,
                        destination != null ? destination.toURIWithoutName() : null,
                        reference,
                        // if ignoreSource is true, set source to null, otherwise use it if it
                        // exists
                        (ignoreSource || source == null) ? null : source.toURIWithoutName(),
                        memo,
                        dueDate));
    }

    @JsonIgnore
    public boolean belongsTo(String accountId) {
        return accountId.equalsIgnoreCase(fromAccountId);
    }

    @JsonIgnore
    public AccountIdentifier getSource() {
        if (Strings.isNullOrEmpty(fromAccountNumber)) {
            return null;
        }

        SwedishIdentifier sourceAccount = new SwedishIdentifier(fromAccountNumber);
        return AccountIdentifier.create(URI.create(sourceAccount.toUriAsString()));
    }

    @JsonIgnore
    public AccountIdentifier getDestination() {
        if (Strings.isNullOrEmpty(recipientAccountNumber)) {
            return null;
        }

        if (getTypeForHash() == TransferType.BANK_TRANSFER) {
            SwedishIdentifier recipientAccount = new SwedishIdentifier(recipientAccountNumber);
            return AccountIdentifier.create(URI.create(recipientAccount.toUriAsString()));
        }

        BankGiroIdentifier bankGiroIdentifier = new BankGiroIdentifier(recipientAccountNumber);
        return AccountIdentifier.create(URI.create(bankGiroIdentifier.toUriAsString()));
    }

    @JsonIgnore
    private TransferType getTypeForHash() {
        switch (type.toLowerCase()) {
            case IcaBankenConstants.AccountTypes.PAYMENT:
            case IcaBankenConstants.AccountTypes.PAYMENT_BG:
            case IcaBankenConstants.AccountTypes.PAYMENT_PG:
                return TransferType.PAYMENT;
            default:
                return TransferType.BANK_TRANSFER;
        }
    }

    public boolean isRejected() {
        return isRejected;
    }

    public String getEventId() {
        return eventId;
    }

    public String getType() {
        return type;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getFromAccountName() {
        return fromAccountName;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getDrawDate() {
        return drawDate;
    }

    public String getMemo() {
        return memo;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getReference() {
        return reference;
    }

    public boolean isStandingTransaction() {
        return isStandingTransaction;
    }

    public boolean isEgiroHasLink() {
        return egiroHasLink;
    }

    public boolean isUpdateable() {
        return isUpdateable;
    }

    public String getFormattedRate() {
        return formattedRate;
    }

    public double getRate() {
        return rate;
    }

    public double getAmountInGivenCurrency() {
        return amountInGivenCurrency;
    }

    public double getApproximateAmount() {
        return approximateAmount;
    }

    public double getTransferCost() {
        return transferCost;
    }

    public double getAmountWithoutTransferCost() {
        return amountWithoutTransferCost;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public boolean isRejectedIsViewed() {
        return rejectedIsViewed;
    }
}

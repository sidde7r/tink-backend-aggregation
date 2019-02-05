package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssignmentEntity {
    @JsonProperty("RegistrationId")
    private String registrationId;
    @JsonProperty("IsRejected")
    private boolean isRejected;
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
    @JsonProperty("Amount")
    private double amount;
    @JsonProperty("DueDate")
    private String dueDate;
    @JsonProperty("DrawDate")
    private String drawDate;
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

    public String getRegistrationId() {
        return registrationId;
    }

    public boolean isRejected() {
        return isRejected;
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

    public String getDueDate() {
        return dueDate;
    }

    public String getDrawDate() {
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

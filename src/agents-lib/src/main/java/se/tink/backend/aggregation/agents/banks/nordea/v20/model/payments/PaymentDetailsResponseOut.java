package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Date;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;
import se.tink.libraries.social.security.SocialSecurityNumber;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetailsResponseOut {
    public static final Function<PaymentDetailsResponseOut, Transfer> TO_EINVOICE_TRANSFER =
            new Function<PaymentDetailsResponseOut, Transfer>() {
                @Nullable
                @Override
                public Transfer apply(@Nullable PaymentDetailsResponseOut paymentDetailsResponseOut) {
                    if (paymentDetailsResponseOut == null) {
                        return null;
                    }

                    return paymentDetailsResponseOut.toEInvoiceTransfer();
                }
            };

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String paymentId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String paymentSubType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String paymentSubTypeExtension;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String fromAccountId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String toAccountId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private double amount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date dueDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date paymentDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String beneficiaryName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String messageRow;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String recurringFrequency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Integer.class)
    private Integer recurringNumberOfPayments;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String statusCode;

    @JsonProperty("recurringContinuously")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean recurringContinuously;

    @JsonProperty("allowedToModify") // Weird "Yes"/"No" String value instead of true/false
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean allowedToModify;

    @JsonProperty("isAllowedToModifyFromAccountId")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean allowedToModifyFromAccountId;

    @JsonProperty("isAllowedToModifyAmount")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean allowedToModifyAmount;

    @JsonProperty("isAllowedToModifyDueDate")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean allowedToModifyDueDate;

    @JsonProperty("isAllowedToModifyMessage")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean allowedToModifyMessage;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String toAccountNumber;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Payment.SubType getPaymentSubType() {
        return Payment.SubType.fromSerializedValue(paymentSubType);
    }

    public void setPaymentSubType(String paymentSubType) {
        this.paymentSubType = paymentSubType;
    }

    public Payment.SubTypeExtension getPaymentSubTypeExtension() {
        return Payment.SubTypeExtension.fromSerializedValue(paymentSubTypeExtension);
    }

    public void setPaymentSubTypeExtension(String paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getMessageRow() {
        return messageRow;
    }

    public void setMessageRow(String messageRow) {
        this.messageRow = messageRow;
    }

    public String getRecurringFrequency() {
        return recurringFrequency;
    }

    public void setRecurringFrequency(String recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }

    public Integer getRecurringNumberOfPayments() {
        return recurringNumberOfPayments;
    }

    public void setRecurringNumberOfPayments(Integer recurringNumberOfPayments) {
        this.recurringNumberOfPayments = recurringNumberOfPayments;
    }

    public Payment.StatusCode getStatusCode() {
        return Payment.StatusCode.fromSerializedValue(statusCode);
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean isRecurringContinuously() {
        return recurringContinuously;
    }

    public void setRecurringContinuously(Boolean recurringContinuously) {
        this.recurringContinuously = recurringContinuously;
    }

    public Boolean isAllowedToModify() {
        return allowedToModify;
    }

    public void setAllowedToModify(Boolean allowedToModify) {
        this.allowedToModify = allowedToModify;
    }

    public Boolean isAllowedToModifyFromAccountId() {
        return allowedToModifyFromAccountId;
    }

    public void setAllowedToModifyFromAccountId(Boolean allowedToModifyFromAccountId) {
        this.allowedToModifyFromAccountId = allowedToModifyFromAccountId;
    }

    public Boolean isAllowedToModifyAmount() {
        return allowedToModifyAmount;
    }

    public void setAllowedToModifyAmount(Boolean allowedToModifyAmount) {
        this.allowedToModifyAmount = allowedToModifyAmount;
    }

    public Boolean isAllowedToModifyDueDate() {
        return allowedToModifyDueDate;
    }

    public void setAllowedToModifyDueDate(Boolean allowedToModifyDueDate) {
        this.allowedToModifyDueDate = allowedToModifyDueDate;
    }

    public Boolean isAllowedToModifyMessage() {
        return allowedToModifyMessage;
    }

    public void setAllowedToModifyMessage(Boolean allowedToModifyMessage) {
        this.allowedToModifyMessage = allowedToModifyMessage;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    @JsonIgnore
    public Transfer toEInvoiceTransfer() {
        Preconditions.checkState(Objects.equal(getPaymentSubType(), Payment.SubType.EINVOICE));
        Preconditions.checkState(Objects.equal(getCurrency(), "SEK"), "Non-SEK eInvoice currency");

        Transfer transfer = new Transfer();
        Payment.StatusCode statusCode = getStatusCode();
        transfer.setType(statusCode == Payment.StatusCode.CONFIRMED ? TransferType.PAYMENT : TransferType.EINVOICE);
        transfer.setDestination(getDestination());
        transfer.setAmount(Amount.inSEK(getAmount()));
        transfer.setSource(getTransferSource());
        transfer.setDestinationMessage(getMessageRow());

        if (paymentDate != null) {
            transfer.setDueDate(DateUtils.flattenTime(paymentDate));
        } else {
            Preconditions.checkNotNull(dueDate,
                    String.format("Non-valid Transfer? PaymentDetailsResponse has dueDate == null: %s.", this));
            transfer.setDueDate(DateUtils.flattenTime(dueDate));
        }

        // Just use the destination name if available for the message on source
        transfer.setSourceMessage(transfer.getDestination().getName().orElse(null));

        return transfer;
    }

    @JsonIgnore
    private AccountIdentifier getDestination() {
        Payment.SubTypeExtension subTypeExtension = getPaymentSubTypeExtension();
        Preconditions.checkNotNull(subTypeExtension,
                "Could not parse sub type extension for einvoice: " + paymentSubTypeExtension);

        return AccountIdentifier.create(subTypeExtension.getType(), getToAccountNumber(), getBeneficiaryName());
    }

    /**
     * Example: NDEASESSXXX-SE1-SEK-8607011234
     */
    @JsonIgnore
    private AccountIdentifier getTransferSource() {
        String internalIdentifier = StringUtils.substringAfterLast(getFromAccountId(), "-");

        if (new SocialSecurityNumber.Sweden(internalIdentifier).isValid()) {
            return new SwedishIdentifier("3300" + internalIdentifier);
        } else {
            return new SwedishIdentifier(internalIdentifier);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("paymentSubType", paymentSubType)
                .add("paymentSubTypeExtension", paymentSubTypeExtension)
                .add("toAccountId", toAccountId)
                .add("amount", amount)
                .add("currency", currency)
                .add("dueDate", dueDate)
                .add("paymentDate", paymentDate)
                .add("messageRow", messageRow)
                .add("recurringFrequency", recurringFrequency)
                .add("recurringNumberOfPayments", recurringNumberOfPayments)
                .add("statusCode", statusCode)
                .add("recurringContinuously", recurringContinuously)
                .add("allowedToModify", allowedToModify)
                .add("allowedToModifyFromAccountId", allowedToModifyFromAccountId)
                .add("allowedToModifyAmount", allowedToModifyAmount)
                .add("allowedToModifyDueDate", allowedToModifyDueDate)
                .add("allowedToModifyMessage", allowedToModifyMessage)
                .add("toAccountNumber", toAccountNumber)
                .toString();
    }
}

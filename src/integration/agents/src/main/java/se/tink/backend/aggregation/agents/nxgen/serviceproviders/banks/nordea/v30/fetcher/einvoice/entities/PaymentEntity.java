package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.PaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.social.security.SocialSecurityNumber;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class PaymentEntity {
    @JsonProperty private String id;
    @JsonProperty private String from;

    @JsonProperty("from_currency")
    private String fromCurrency;

    @JsonProperty("to")
    private String recipientAccountNumber;

    @JsonProperty("to_account_number_type")
    private String toAccountNumberType;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty private String message;

    @JsonProperty("own_message")
    private String ownMessage;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty private String reference;
    @JsonProperty private double amount;
    @JsonProperty private String status;
    @JsonProperty private String type;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty
    private Date due;

    @JsonProperty private String currency;

    @JsonProperty("e_invoice")
    private EInvoiceEntity eInvoice;

    @JsonProperty private PermissionsEntity permissions;

    @JsonIgnore
    public String getId() {
        return id;
    }

    @JsonIgnore
    public boolean isNotPlusgiro() {
        return !type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.PLUSGIRO);
    }

    @JsonIgnore
    public Transfer toTinkTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.of(amount, NordeaBaseConstants.CURRENCY));
        transfer.setType(getTinkType());
        transfer.setSource(getSource());
        transfer.setDestination(getDestination());
        transfer.setDueDate(due);
        transfer.setDestinationMessage(getDestinationMessage());
        transfer.setSourceMessage(recipientName);
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, getApiIdentifier());

        return transfer;
    }

    @JsonIgnore
    public TransferType getTinkType() {
        switch (type.toLowerCase()) {
            case NordeaBaseConstants.PaymentTypes.BANKGIRO:
            case NordeaBaseConstants.PaymentTypes.PLUSGIRO:
                if (hasEIvoiceDetails()) {
                    return TransferType.EINVOICE;
                } else {
                    return TransferType.PAYMENT;
                }
            case NordeaBaseConstants.PaymentTypes.EINVOICE:
                return TransferType.EINVOICE;
            case NordeaBaseConstants.PaymentTypes.IBAN:
            case NordeaBaseConstants.PaymentTypes.LBAN:
            case NordeaBaseConstants.PaymentTypes.NORMAL:
                return TransferType.BANK_TRANSFER;
            default:
                throw new IllegalArgumentException(
                        NordeaBaseConstants.PaymentTypes.UNKNOWN_TYPE + ' ' + type);
        }
    }

    @JsonIgnore
    private AccountIdentifier getDestination() {

        AccountIdentifier destination =
                AccountIdentifier.create(
                        getRecipientTinkType(), recipientAccountNumber, getDestinationName());
        if (destination.is(AccountIdentifierType.SE_NDA_SSN)) {
            return destination.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        }
        return destination;
    }

    @JsonIgnore
    private AccountIdentifier getSource() {
        AccountIdentifier ssnIdentifier =
                AccountIdentifier.create(AccountIdentifierType.SE_NDA_SSN, from);
        if (ssnIdentifier.isValid()) {
            return ssnIdentifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return AccountIdentifier.create(AccountIdentifierType.SE, from);
        }
    }

    // plusgiro does not seem to have an id but it has a reference field instead.
    @JsonIgnore
    public String getApiIdentifier() {
        return Strings.isNullOrEmpty(id) ? reference : id;
    }

    @JsonIgnore
    private String getDestinationName() {
        return Strings.isNullOrEmpty(recipientName) ? bankName : recipientName;
    }

    @JsonIgnore
    private AccountIdentifierType getRecipientTinkType() {
        switch (toAccountNumberType.toUpperCase()) {
            case NordeaBaseConstants.PaymentAccountTypes.BANKGIRO:
                return AccountIdentifierType.SE_BG;
            case NordeaBaseConstants.PaymentAccountTypes.PLUSGIRO:
                return AccountIdentifierType.SE_PG;
            case NordeaBaseConstants.PaymentAccountTypes.LBAN:
                return AccountIdentifierType.SE;
            case NordeaBaseConstants.PaymentAccountTypes.NDASE:
                if (new SocialSecurityNumber.Sweden(recipientAccountNumber).isValid()) {
                    return AccountIdentifierType.SE_NDA_SSN;
                } else {
                    return AccountIdentifierType.SE;
                }
            default:
                throw new IllegalArgumentException(
                        NordeaBaseConstants.PaymentAccountTypes.UNKNOWN_ACCOUNT_TYPE
                                + ' '
                                + toAccountNumberType);
        }
    }

    @JsonIgnore
    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    @JsonIgnore
    public String getFrom() {
        return from;
    }

    @JsonIgnore
    public String getType() {
        return type;
    }

    @JsonIgnore
    public boolean isRejected() {
        return status.equalsIgnoreCase(PaymentStatus.REJECTED);
    }

    @JsonIgnore
    public boolean isConfirmed() {
        return status.equalsIgnoreCase(PaymentStatus.CONFIRMED);
    }

    @JsonIgnore
    public boolean isEInvoice() {
        return type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.EINVOICE);
    }

    // return true if type is bankgiro or plusgiro
    @JsonIgnore
    public boolean isPayment() {
        return (type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.PLUSGIRO)
                || type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.BANKGIRO));
    }

    @JsonIgnore
    public boolean isTransfer() {
        return (type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.LBAN)
                || type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.IBAN)
                || type.equalsIgnoreCase(NordeaBaseConstants.PaymentTypes.NORMAL));
    }

    @JsonIgnore
    public boolean isUnconfirmed() {
        return status.equalsIgnoreCase(PaymentStatus.UNCONFIRMED);
    }

    public boolean isInProgress() {
        return status.equalsIgnoreCase(PaymentStatus.INPROGRESS);
    }

    @JsonIgnore
    public Date getDue() {
        return due;
    }

    @JsonIgnore
    public Amount getAmount() {
        return new Amount(NordeaBaseConstants.CURRENCY, amount);
    }

    @JsonIgnore
    private String getDestinationMessage() {
        // einvoce has a message field for OCR. Plusgiro does not have a message field
        return type.equals(NordeaBaseConstants.PaymentTypes.PLUSGIRO) ? recipientName : message;
    }

    @JsonIgnore
    public boolean isEqualToTransfer(Transfer transfer, Date transferDueDate) {
        return transfer.getAmount().equals(getAmount())
                && isIdentifierEquals(transfer.getDestination(), getRecipientAccountNumber())
                && isIdentifierEquals(transfer.getSource(), getFrom())
                && DateUtils.isSameDay(transferDueDate, getDue())
                && Strings.nullToEmpty(transfer.getRemittanceInformation().getValue())
                        .equals(Strings.nullToEmpty(message));
    }

    private static boolean isIdentifierEquals(AccountIdentifier identifier, String accountNumber) {
        return identifier
                .getIdentifier(new NordeaAccountIdentifierFormatter())
                .equals(accountNumber);
    }

    @JsonIgnore
    public String getMessage() {
        return message;
    }

    @JsonIgnore
    public PermissionsEntity getPermissions() {
        return Optional.ofNullable(permissions).orElse(new PermissionsEntity());
    }

    @JsonIgnore
    public boolean hasEIvoiceDetails() {
        return Objects.nonNull(eInvoice);
    }

    @JsonIgnore
    public UpcomingTransaction toUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setDescription(getDestinationName())
                .setDate(due)
                .setAmount(ExactCurrencyAmount.inSEK(-1.0 * amount))
                .build();
    }

    @JsonIgnore
    public String getReference() {
        return reference;
    }

    @JsonIgnore
    public String getOwnMessage() {
        return ownMessage;
    }

    @JsonIgnore
    public String getBankName() {
        return bankName;
    }

    @JsonIgnore
    public String getRecipientName() {
        return recipientName;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

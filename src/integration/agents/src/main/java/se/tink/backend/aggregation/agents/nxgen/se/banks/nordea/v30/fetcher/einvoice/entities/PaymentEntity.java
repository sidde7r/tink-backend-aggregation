package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.PaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
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

    @JsonIgnore
    public String getId() {
        return Optional.ofNullable(id).get();
    }

    @JsonIgnore
    public boolean isNotPlusgiro() {
        return !type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.PLUSGIRO);
    }

    @JsonIgnore
    public Transfer toTinkTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(new Amount(NordeaSEConstants.CURRENCY, amount));
        transfer.setType(getTinkType());
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
            case NordeaSEConstants.PaymentTypes.BANKGIRO:
            case NordeaSEConstants.PaymentTypes.PLUSGIRO:
                return TransferType.PAYMENT;
            case NordeaSEConstants.PaymentTypes.EINVOICE:
                return TransferType.EINVOICE;
            case NordeaSEConstants.PaymentTypes.IBAN:
            case NordeaSEConstants.PaymentTypes.LBAN:
            case NordeaSEConstants.PaymentTypes.NORMAL:
                return TransferType.BANK_TRANSFER;
            default:
                throw new IllegalArgumentException(
                        NordeaSEConstants.PaymentTypes.UNKNOWN_TYPE + ' ' + type);
        }
    }

    @JsonIgnore
    public AccountIdentifier getDestination() {
        return AccountIdentifier.create(
                getTinkPaymentType(), getRecipientTransferAccountNumber(), getRecipientName());
    }

    public String getRecipientTransferAccountNumber() {
        if (NordeaSEConstants.PaymentAccountTypes.NDASE.equalsIgnoreCase(toAccountNumberType)) {
            return getTransferAccountNumber(recipientAccountNumber);
        }
        return recipientAccountNumber;
    }

    private static String getTransferAccountNumber(String accountNumber) {
        SocialSecurityNumber.Sweden pnr = new SocialSecurityNumber.Sweden(accountNumber);
        if (pnr.isValid()) {
            return NordeaSEConstants.TransactionalAccounts.NORDEA_CLEARING_NUMBER + accountNumber;
        }
        return accountNumber;
    }

    // plusgiro does not seem to have an id but it has a reference field instead.
    @JsonIgnore
    public String getApiIdentifier() {
        return Strings.isNullOrEmpty(id) ? reference : id;
    }

    @JsonIgnore
    private String getRecipientName() {
        return toAccountNumberType.equalsIgnoreCase(NordeaSEConstants.PaymentAccountTypes.LBAN)
                ? bankName
                : recipientName;
    }

    @JsonIgnore
    private AccountIdentifier.Type getTinkPaymentType() {
        switch (toAccountNumberType.toUpperCase()) {
            case NordeaSEConstants.PaymentAccountTypes.BANKGIRO:
                return AccountIdentifier.Type.SE_BG;
            case NordeaSEConstants.PaymentAccountTypes.PLUSGIRO:
                return AccountIdentifier.Type.SE_PG;
            case NordeaSEConstants.PaymentAccountTypes.LBAN:
            case NordeaSEConstants.PaymentAccountTypes.NDASE:
                return AccountIdentifier.Type.SE;
            default:
                throw new IllegalArgumentException(
                        NordeaSEConstants.PaymentAccountTypes.UNKNOWN_ACCOUNT_TYPE
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
        return type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.EINVOICE);
    }

    // return true if type is bankgiro or plusgiro
    @JsonIgnore
    public boolean isPayment() {
        return (type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.PLUSGIRO)
                || type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.BANKGIRO));
    }

    @JsonIgnore
    public boolean isTransfer() {
        return (type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.LBAN)
                || type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.IBAN)
                || type.equalsIgnoreCase(NordeaSEConstants.PaymentTypes.NORMAL));
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
        return new Amount(NordeaSEConstants.CURRENCY, amount);
    }

    @JsonIgnore
    private String getDestinationMessage() {
        // einvoce has a message field for OCR. Plusgiro does not have a message field
        return type.equals(NordeaSEConstants.PaymentTypes.PLUSGIRO) ? recipientName : message;
    }

    @JsonIgnore
    public boolean isEqualToTransfer(Transfer transfer) {
        return transfer.getAmount().equals(getAmount())
                && transfer.getDestination().getIdentifier().equals(getRecipientAccountNumber())
                && transfer.getSource().getIdentifier().equals(getTransferAccountNumber(getFrom()))
                && DateUtils.isSameDay(transfer.getDueDate(), getDue())
                && Strings.nullToEmpty(transfer.getDestinationMessage())
                        .equals(Strings.nullToEmpty(message));
    }

    @JsonIgnore
    public boolean hasEIvoiceDetails() {
        return Objects.nonNull(eInvoice);
    }

    @JsonIgnore
    public UpcomingTransaction toUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setDescription(recipientName)
                .setDate(due)
                .setAmount(Amount.inSEK(-1.0 * amount))
                .build();
    }
}

package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.banks.se.icabanken.ICABankenUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceEntity {
    @JsonProperty("UniqueId")
    private String uuid;

    @JsonProperty("RecipientName")
    private String name;

    @JsonProperty("RecipientType")
    private String type;

    @JsonProperty("RecipientAccountNumber")
    private String accountNumber;

    @JsonProperty("PayDate")
    private String date;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("IsRecipientConfirmed")
    private boolean recipientConfirmed;

    public Transfer toTinkTransfer(Catalog catalog) {
        AccountIdentifier destination = getIdentifier();

        Transfer transfer = new Transfer();

        String uuid = getUuid();
        Preconditions.checkState(
                !Strings.isNullOrEmpty(uuid), "Could not find Unique ID for invoice");
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, uuid);

        transfer.setAmount(Amount.inSEK(getAmount()));
        transfer.setDueDate(DateUtils.flattenTime(DateUtils.parseDate(getDate())));
        transfer.setDestination(destination);
        transfer.setType(TransferType.EINVOICE);
        transfer.setSourceMessage(name);

        // ICA Banken doesn't supply a message or OCR to us, but the app requires that we fill in
        // that field to confirm
        // the payment. Using a prepopulated value here. In ICABankenAgent we later make sure that
        // the user hasn't
        // changes this field.
        transfer.setDestinationMessage(catalog.getString("N/A"));

        return transfer;
    }

    private AccountIdentifier getIdentifier() {

        AccountIdentifier.Type type = ICABankenUtils.paymentTypeToIdentifierType(getType());
        Preconditions.checkNotNull(type, "Invalid identifier type. It must not be null.");

        String accountNumber = getAccountNumber();
        Preconditions.checkState(
                !Strings.isNullOrEmpty(accountNumber), "No destination accountNumber");

        return AccountIdentifier.create(type, accountNumber, name);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean isRecipientConfirmed() {
        return recipientConfirmed;
    }

    public void setRecipientConfirmed(boolean recipientConfirmed) {
        this.recipientConfirmed = recipientConfirmed;
    }
}

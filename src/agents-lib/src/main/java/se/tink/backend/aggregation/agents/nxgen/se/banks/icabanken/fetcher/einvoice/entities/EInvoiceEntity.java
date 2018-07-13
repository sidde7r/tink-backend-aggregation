package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.banks.se.icabanken.ICABankenUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;

@JsonObject
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
        Preconditions.checkState(!Strings.isNullOrEmpty(uuid), "Could not find Unique ID for invoice");
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, uuid);

        transfer.setAmount(Amount.inSEK(getAmount()));
        transfer.setDueDate(DateUtils.flattenTime(DateUtils.parseDate(getDate())));
        transfer.setDestination(destination);
        transfer.setType(TransferType.EINVOICE);
        transfer.setSourceMessage(name);

        // ICA Banken doesn't supply a message or OCR to us, but the app requires that we fill in that field to confirm
        // the payment. Using a prepopulated value here. In ICABankenAgent we later make sure that the user hasn't
        // changes this field.
        transfer.setDestinationMessage(catalog.getString(IcaBankenConstants.IdTags.NOT_AVAILABLE_TAG));

        return transfer;
    }

    private AccountIdentifier getIdentifier() {

        AccountIdentifier.Type type = ICABankenUtils.paymentTypeToIdentifierType(getType());
        Preconditions.checkNotNull(type, "Invalid identifier type. It must not be null.");

        String accountNumber = getAccountNumber();
        Preconditions.checkState(!Strings.isNullOrEmpty(accountNumber), "No destination accountNumber");

        return AccountIdentifier.create(type, accountNumber, name);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getDate() {
        return date;
    }

    public Double getAmount() {
        return amount;
    }

    public boolean isRecipientConfirmed() {
        return recipientConfirmed;
    }
}

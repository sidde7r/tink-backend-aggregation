package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorUtils;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class EInvoiceEntity {
    @JsonProperty("UniqueId")
    private String uniqueId;

    @JsonProperty("RecipientName")
    private String recipientName;

    @JsonProperty("RecipientType")
    private String recipientType;

    @JsonProperty("RecipientAccountNumber")
    private String recipientAccountNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("PayDate")
    private Date payDate;

    @JsonDouble
    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("IsChangeableAmount")
    private boolean isChangeableAmount;

    @JsonProperty("HasLink")
    private boolean hasLink;

    @JsonProperty("InvoiceLink")
    private String invoiceLink;

    @JsonProperty("IsRecipientConfirmed")
    private boolean isRecipientConfirmed;

    // This method is directly moved over from the old ICABanken agent.
    @JsonIgnore
    public Transfer toTinkTransfer(Catalog catalog) {
        AccountIdentifier destination = getIdentifier();

        Transfer transfer = new Transfer();

        Preconditions.checkState(
                !Strings.isNullOrEmpty(uniqueId), "Could not find Unique ID for invoice");
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, uniqueId);

        transfer.setAmount(ExactCurrencyAmount.inSEK(getAmount()));
        transfer.setDueDate(payDate);
        transfer.setDestination(destination);
        transfer.setType(TransferType.EINVOICE);
        transfer.setSourceMessage(recipientName);

        // ICA Banken doesn't supply a message or OCR to us, but the app requires that we fill in
        // that field to confirm
        // the payment. Using a prepopulated value here. We later make sure that the user hasn't
        // changed this field.
        transfer.setDestinationMessage(
                catalog.getString(IcaBankenConstants.IdTags.NOT_AVAILABLE_TAG));

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue(
                catalog.getString(IcaBankenConstants.IdTags.NOT_AVAILABLE_TAG));
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }

    @JsonIgnore
    private AccountIdentifier getIdentifier() {

        AccountIdentifier.Type type =
                IcaBankenExecutorUtils.paymentTypeToIdentifierType(recipientType);
        Preconditions.checkNotNull(type, "Invalid identifier type. It must not be null.");

        Preconditions.checkState(
                !Strings.isNullOrEmpty(recipientAccountNumber), "No destination accountNumber");

        return AccountIdentifier.create(type, recipientAccountNumber, recipientName);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public Date getPayDate() {
        return payDate;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isChangeableAmount() {
        return isChangeableAmount;
    }

    public boolean isHasLink() {
        return hasLink;
    }

    public String getInvoiceLink() {
        return invoiceLink;
    }

    public boolean isRecipientConfirmed() {
        return isRecipientConfirmed;
    }
}

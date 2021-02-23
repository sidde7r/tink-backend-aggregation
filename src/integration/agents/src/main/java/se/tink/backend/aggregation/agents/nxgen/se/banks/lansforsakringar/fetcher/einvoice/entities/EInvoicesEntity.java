package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.util.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class EInvoicesEntity {
    private String eInvoiceId;
    private String invoiceType;
    private String invoiceStatus;
    private String paymentType;
    private String eInvoiceIssuerName;
    private String eInvoiceIssuerNumber;
    private String giroType;
    private String giroNumber;
    private String ocrNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    private String payDate;
    private String currency;
    private int defaultMinimumAmount;
    private int actualAmountToPay;
    private String changeableAmount;
    // `accountNumber` is null - cannot define it!
    private String informationUrl;
    private String externalInvoiceUrl;

    @JsonIgnore
    public Transfer toTinkTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(actualAmountToPay));
        transfer.setType(TransferType.EINVOICE);
        transfer.setDestination(getDestination());
        transfer.setDueDate(dueDate);
        transfer.setDestinationMessage(ocrNumber);
        transfer.setSourceMessage(eInvoiceIssuerName);
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, eInvoiceId);
        return transfer;
    }

    private AccountIdentifier getDestination() {
        Preconditions.checkState(!Strings.isNullOrEmpty(giroType), "Null or empty giro type");
        if (giroType.equalsIgnoreCase("bg")) {
            // Wouldn't BankGiroIdentifier be a better choice?
            return AccountIdentifier.create(Type.SE_BG, giroNumber, eInvoiceIssuerName);
        } else {
            return AccountIdentifier.create(Type.SE_PG, giroNumber, eInvoiceIssuerName);
        }
    }

    public String geteInvoiceId() {
        return eInvoiceId;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String geteInvoiceIssuerName() {
        return eInvoiceIssuerName;
    }

    public String geteInvoiceIssuerNumber() {
        return eInvoiceIssuerNumber;
    }

    public String getGiroType() {
        return giroType;
    }

    public String getGiroNumber() {
        return giroNumber;
    }

    public String getOcrNumber() {
        return ocrNumber;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getPayDate() {
        return payDate;
    }

    public String getCurrency() {
        return currency;
    }

    public int getDefaultMinimumAmount() {
        return defaultMinimumAmount;
    }

    public int getActualAmountToPay() {
        return actualAmountToPay;
    }

    public String getChangeableAmount() {
        return changeableAmount;
    }

    public String getInformationUrl() {
        return informationUrl;
    }

    public String getExternalInvoiceUrl() {
        return externalInvoiceUrl;
    }
}

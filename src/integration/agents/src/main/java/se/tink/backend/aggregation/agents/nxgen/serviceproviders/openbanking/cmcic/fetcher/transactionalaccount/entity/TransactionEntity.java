package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("transactionAmount")
    private AmountTypeEntity transactionAmount = null;

    @JsonProperty("creditDebitIndicator")
    private CreditDebitIndicatorEntity creditDebitIndicator = null;

    @JsonProperty("status")
    private TransactionStatusEntity status = null;

    @JsonProperty("bookingDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate = null;

    @JsonProperty("valueDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate = null;

    @JsonProperty("transactionDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate = null;

    @JsonProperty("remittanceInformation")
    private RemittanceInformationEntity remittanceInformation = null;

    public Transaction toTinkTransaction(TransactionalAccount account) {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDate(bookingDate)
                .setPending(status == TransactionStatusEntity.PDNG)
                .setDescription(String.join(" ", remittanceInformation.getUnstructured()))
                .build();
    }

    private Amount getAmount() {
        return new Amount(
                transactionAmount.getCurrency(), Double.parseDouble(transactionAmount.getAmount()));
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public AmountTypeEntity getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(AmountTypeEntity transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public CreditDebitIndicatorEntity getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public void setCreditDebitIndicator(CreditDebitIndicatorEntity creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
    }

    public TransactionStatusEntity getStatus() {
        return status;
    }

    public void setStatus(TransactionStatusEntity status) {
        this.status = status;
    }

    public RemittanceInformationEntity getRemittanceInformation() {
        return remittanceInformation;
    }

    public void setRemittanceInformation(RemittanceInformationEntity remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String bookingDate;
    private DebtorAccountEntity debtorAccount;
    private String entryReference;
    private String remittanceInformationUnstructured;
    private BalanceAmountEntity transactionAmount;
    private Date transactionDate;
    private String merchantName;
    private String text;

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public BalanceAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getText() {
        return text;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public DebtorAccountEntity getDebtorAccount() {
        return debtorAccount;
    }

    public String getEntryReference() {
        return entryReference;
    }

    @JsonIgnore
    private String getDescription() {
        if (remittanceInformationUnstructured != null) {
            return remittanceInformationUnstructured;
        }
        return merchantName != null ? merchantName : text;
    }

    @JsonIgnore
    public Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setAmount(transactionAmount.getAmount())
                .setDate(transactionDate)
                .setDescription(getDescription())
                .setPending(pending)
                .build();
    }

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return toTinkTransaction(false);
    }

    @JsonIgnore
    public Transaction toPendinginkTransaction() {
        return toTinkTransaction(true);
    }
}

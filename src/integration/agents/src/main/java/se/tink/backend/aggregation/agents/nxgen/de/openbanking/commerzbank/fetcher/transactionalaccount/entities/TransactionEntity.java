package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private AccountsEntity debtorAccount;
    private String creditorName;
    private TransactionAmountEntity transactionAmount;
    private AccountsEntity creditorAccount;
    private Date bookingDate;
    private String valueDate;
    private String remittanceInformationUnstructured;
    private String endToEndId;
    private String debtorName;

    public AccountsEntity getDebtorAccount() {
        return debtorAccount;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public AccountsEntity getCreditorAccount() {
        return creditorAccount;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public String getDebtorName() {
        return debtorName;
    }

    @JsonIgnore
    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(false)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toAmount())
                .build();
    }

    @JsonIgnore
    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setDate(bookingDate)
                .setPending(true)
                .setDescription(getDescription())
                .setAmount(transactionAmount.toAmount())
                .build();
    }

    private String getDescription() {
        if (!Strings.isNullOrEmpty(remittanceInformationUnstructured)) {
            return remittanceInformationUnstructured;
        }
        if (!Strings.isNullOrEmpty(debtorName)) {
            return debtorName;
        }
        return creditorName;
    }
}

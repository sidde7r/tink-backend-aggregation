package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferTransactionGroupEntity {
    private String amount;
    private List<TransferTransactionEntity> transactions;
    private TransactionAccountEntity fromAccount;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<TransferTransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<TransferTransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public TransactionAccountEntity getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(TransactionAccountEntity fromAccount) {
        this.fromAccount = fromAccount;
    }
}

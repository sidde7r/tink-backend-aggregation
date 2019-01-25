package se.tink.backend.aggregation.agents.creditcards.okq8.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.system.rpc.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    @JsonProperty("account_data")
    private AccountDataEntity accountData;
    @JsonProperty("transactions_data")
    private List<TransactionDataEntity> transactionsData = Lists.newArrayList();

    public AccountDataEntity getAccountData() {
        return accountData;
    }

    public void setAccountData(AccountDataEntity accountData) {
        this.accountData = accountData;
    }

    public List<TransactionDataEntity> getTransactionsData() {
        return transactionsData;
    }

    public void setTransactionsData(List<TransactionDataEntity> transactionsData) {
        this.transactionsData = transactionsData;
    }

    public List<Transaction> getTransactionsDataAsTinkTransactions() {
        if (transactionsData == null) {
            return null;
        }

        return Lists.newArrayList(Lists
                .transform(transactionsData, TransactionDataEntity.TO_TINK_TRANSACTION_TRANSFORM));
    }
}

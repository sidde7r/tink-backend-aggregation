package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse extends AbstractResponse {
    @JsonProperty("LastId")
    private String lastId;

    @JsonProperty("MoreTransactions")
    private boolean moreTransactions;

    @JsonProperty("Transactions")
    private List<TransactionEntity> transactions;

    public String getLastId() {
        return lastId;
    }

    /** We don't use deleted transactions for anything in Tink, so let's just remove them. */
    public List<TransactionEntity> getTransactions() {
        if (transactions == null) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(
                FluentIterable.from(transactions)
                        .filter(Predicates.not(TransactionEntity.IS_DELETED_OR_REJECTED)));
    }

    public boolean isMoreTransactions() {
        return moreTransactions;
    }

    public void setLastId(String lastId) {
        this.lastId = lastId;
    }

    public void setMoreTransactions(boolean moreTransactions) {
        this.moreTransactions = moreTransactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}

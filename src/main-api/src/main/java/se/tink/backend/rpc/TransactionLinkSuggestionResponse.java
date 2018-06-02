package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.Transaction;

public class TransactionLinkSuggestionResponse {
    @Tag(1)
    @ApiModelProperty(name = "transactionId", value="The id of the transaction to find suggestions for.")
    private String transactionId;
    @Tag(2)
    @ApiModelProperty(name = "limit", value="The maximum amount of suggestions requested to be returned.")
    private int limit;
    @Tag(3)
    @ApiModelProperty(name = "suggestedCounterpartTransactions", value="Suggested counterpart transactions.")
    private List<Transaction> suggestedCounterpartTransactions;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<Transaction> getSuggestedCounterpartTransactions() {
        return suggestedCounterpartTransactions;
    }

    public void setSuggestedCounterpartTransactions(
            List<Transaction> suggestedCounterpartTransactions) {
        this.suggestedCounterpartTransactions = suggestedCounterpartTransactions;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}

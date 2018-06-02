package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.Transaction;

public class LinkTransactionsResponse {
    @Tag(1)
    @ApiModelProperty(name = "transaction", value="The primary transaction.", required = true)
    private Transaction transaction;
    @Tag(2)
    @ApiModelProperty(name = "counterpartTransaction", value="The counterpart transaction.", required = true)
    private Transaction counterpartTransaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getCounterpartTransaction() {
        return counterpartTransaction;
    }

    public void setCounterpartTransaction(Transaction counterpartTransaction) {
        this.counterpartTransaction = counterpartTransaction;
    }
}

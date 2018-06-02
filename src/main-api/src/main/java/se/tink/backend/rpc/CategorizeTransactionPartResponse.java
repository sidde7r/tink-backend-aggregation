package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.Transaction;

public class CategorizeTransactionPartResponse {
    @Tag(1)
    @ApiModelProperty(name = "transaction", value="The transaction to which the re-categorized part belongs.", required = true)
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}

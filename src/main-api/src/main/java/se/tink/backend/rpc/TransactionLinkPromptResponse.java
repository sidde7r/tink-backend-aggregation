package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.Transaction;

public class TransactionLinkPromptResponse {
    @Tag(1)
    @ApiModelProperty(name = "transaction", value="The transaction that the answer applied to.")
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}

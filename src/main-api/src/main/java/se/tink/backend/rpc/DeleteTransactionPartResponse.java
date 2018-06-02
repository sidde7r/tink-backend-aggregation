package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.Transaction;

public class DeleteTransactionPartResponse {
    @Tag(1)
    @ApiModelProperty(name = "transaction", value="The transaction to which the part belonged.", required = true)
    private Transaction transaction;
    @Tag(2)
    @ApiModelProperty(name = "counterpartTransaction", value="Counterpart transaction affected due to bilateral link being removed.")
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

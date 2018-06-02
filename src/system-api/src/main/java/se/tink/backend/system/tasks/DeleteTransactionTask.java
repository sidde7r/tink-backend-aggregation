package se.tink.backend.system.tasks;

import se.tink.backend.system.rpc.DeleteTransactionRequest;

public class DeleteTransactionTask extends Task<DeleteTransactionRequest> {

    public static final String TOPIC = "UPDATE_TRANSACTIONS";

    public DeleteTransactionTask() {
        super(TOPIC);
    }
}

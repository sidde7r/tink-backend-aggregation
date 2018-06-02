package se.tink.backend.system.tasks;

import se.tink.backend.system.rpc.CheckpointRollbackRequest;

public class CheckpointRollbackTask extends Task<CheckpointRollbackRequest> {

    public static final String TOPIC = "UPDATE_TRANSACTIONS";

    public CheckpointRollbackTask() {
        super(TOPIC);
    }
}

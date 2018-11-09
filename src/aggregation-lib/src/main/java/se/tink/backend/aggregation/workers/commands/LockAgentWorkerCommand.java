package se.tink.backend.aggregation.workers.commands;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;

public class LockAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log = new AggregationLogger(LockAgentWorkerCommand.class);
    private static final String LOCK_FORMAT = "/locks/aggregation/LockAgentWorkerCommand/%s/%s"; // % (userId, className)

    private InterProcessLock lock;
    private AgentWorkerCommandContext context;
    private boolean hasAcquiredLock;

    public LockAgentWorkerCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRequest request = context.getRequest();

        // Acquire a lock on userId & provider className.
        // This lock will cover a whole "provider family", e.g. Swedbank/Savingsbank or SEBKortAgent.
        // Note: This is a requisite for the Swedbank/Savingbanks.
        lock = new InterProcessSemaphoreMutex(
                context.getCoordinationClient(),
                String.format(LOCK_FORMAT, request.getUser().getId(), request.getProvider().getClassName())
        );

        hasAcquiredLock = lock.acquire(4, TimeUnit.MINUTES);

        if (!hasAcquiredLock) {
            return AgentWorkerCommandResult.ABORT;
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {
        try {
            // If we never executed the command
            if (lock == null) {
                return;
            }

            // If we didn't aquire this lock (e.g. there is a timeout of 4 minutes)
            if (!hasAcquiredLock) {
                return;
            }

            lock.release();
        } catch (Exception e) {
            log.error("Caught exception while releasing lock", e);
        }
    }
}

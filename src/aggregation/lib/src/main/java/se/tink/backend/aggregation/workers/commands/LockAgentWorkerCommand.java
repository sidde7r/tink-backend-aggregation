package se.tink.backend.aggregation.workers.commands;

import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LockAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(LockAgentWorkerCommand.class);
    private static final String LOCK_FORMAT =
            "/locks/aggregation/LockAgentWorkerCommand/%s/%s"; // % (userId, credentialsId)

    private InterProcessLock lock;
    private AgentWorkerCommandContext context;
    private boolean hasAcquiredLock;

    public LockAgentWorkerCommand(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        CredentialsRequest request = context.getRequest();

        String userId = request.getUser().getId();
        String credentialsId = request.getCredentials().getId();

        lock =
                new InterProcessSemaphoreMutex(
                        context.getCoordinationClient(),
                        String.format(LOCK_FORMAT, userId, credentialsId));

        hasAcquiredLock = lock.acquire(5, TimeUnit.SECONDS);

        if (!hasAcquiredLock) {
            log.info("Could not acquire lock for user: {} credentials: {}", userId, credentialsId);
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

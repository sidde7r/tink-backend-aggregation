package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LockAgentWorkerCommand extends AgentWorkerCommand {
    private static final AggregationLogger log =
            new AggregationLogger(LockAgentWorkerCommand.class);
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

        lock =
                new InterProcessSemaphoreMutex(
                        context.getCoordinationClient(),
                        String.format(
                                LOCK_FORMAT,
                                request.getUser().getId(),
                                request.getCredentials().getId()));

        String providerName = request.getProvider().getName();

        if (!Strings.isNullOrEmpty(providerName) && providerName.startsWith("uk-")) {
            // Super UK specific fix to handle two requests coming in at the same time. We don't
            // want the
            // second request to execute as it works with stale data and will put the credential
            // in AUTHENTICATION_ERROR. This should be removed once we have a robust solution in
            // place.
            hasAcquiredLock = lock.acquire(35, TimeUnit.SECONDS);
        } else {
            hasAcquiredLock = lock.acquire(4, TimeUnit.MINUTES);
        }

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

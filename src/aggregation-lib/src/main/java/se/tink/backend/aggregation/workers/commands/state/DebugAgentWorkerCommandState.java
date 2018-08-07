package se.tink.backend.aggregation.workers.commands.state;

import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.common.ServiceContext;

import java.io.File;

public class DebugAgentWorkerCommandState {
    private File debugDirectory;
    private static final AggregationLogger log = new AggregationLogger(DebugAgentWorkerCommandState.class);

    public DebugAgentWorkerCommandState(ServiceContext serviceContext) {
        debugDirectory = new File(serviceContext.getConfiguration().getAggregationWorker()
                .getDebugLogDir());
        try {
            if (debugDirectory.mkdirs()) {
                log.info("Log directory was created: " + debugDirectory.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not make sure log directory was created: "
                    + debugDirectory.getAbsolutePath(), e);
        }
    }

    public File getDebugDirectory() {
        return debugDirectory;
    }
}

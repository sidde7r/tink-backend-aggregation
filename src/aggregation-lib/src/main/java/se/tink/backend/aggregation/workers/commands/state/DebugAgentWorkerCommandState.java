package se.tink.backend.aggregation.workers.commands.state;

import javax.inject.Inject;
import se.tink.backend.aggregation.log.AggregationLogger;

import java.io.File;
import se.tink.backend.common.config.ServiceConfiguration;

public class DebugAgentWorkerCommandState {
    private File debugDirectory;
    private static final AggregationLogger log = new AggregationLogger(DebugAgentWorkerCommandState.class);

    @Inject
    public DebugAgentWorkerCommandState(ServiceConfiguration configuration) {
        debugDirectory = new File(configuration.getAggregationWorker().getDebugLogDir());
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

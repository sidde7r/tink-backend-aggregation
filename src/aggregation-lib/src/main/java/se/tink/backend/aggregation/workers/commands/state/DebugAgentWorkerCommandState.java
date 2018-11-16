package se.tink.backend.aggregation.workers.commands.state;

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

import java.io.File;

public class DebugAgentWorkerCommandState {
    private static final Logger log = LoggerFactory.getLogger(DebugAgentWorkerCommandState.class);
    private File debugDirectory;

    @Inject
    public DebugAgentWorkerCommandState(AgentsServiceConfiguration configuration) {
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

package se.tink.backend.aggregation.workers.commands.state;

import java.io.File;
import java.util.Objects;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AggregationWorkerConfiguration;

public class DebugAgentWorkerCommandState {
    private static final Logger log = LoggerFactory.getLogger(DebugAgentWorkerCommandState.class);
    private final String debugLogDir;
    private final int debugLogFrequencyPercent;
    private final String longTermStorageDisputeBasePrefix;
    private File debugDirectory;

    @Inject
    public DebugAgentWorkerCommandState(AgentsServiceConfiguration configuration) {
        debugLogDir = getDebugLogDirFromConfig(configuration);
        debugLogFrequencyPercent = getDebugLogFrequencyFromConfig(configuration);
        longTermStorageDisputeBasePrefix =
                getLongTermStorageDisputeBasePrefixFromConfig(configuration);
    }

    public boolean isSaveLocally() {
        return Objects.nonNull(this.debugLogDir);
    }

    public File getDebugDirectory() {
        if (Objects.nonNull(this.debugDirectory)) {
            return this.debugDirectory;
        }

        return createDirectory();
    }

    private File createDirectory() {
        this.debugDirectory = new File(this.debugLogDir);
        try {
            if (this.debugDirectory.mkdirs()) {
                log.info("Log directory was created: " + this.debugDirectory.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not make sure log directory was created: "
                            + this.debugDirectory.getAbsolutePath(),
                    e);
        }

        return this.debugDirectory;
    }

    private String getDebugLogDirFromConfig(AgentsServiceConfiguration configuration) {
        if (Objects.isNull(configuration)
                || Objects.isNull(configuration.getAggregationWorker())
                || Objects.isNull(configuration.getAggregationWorker().getDebugLogDir())
                || Objects.equals(
                        AggregationWorkerConfiguration.DEFAULT_DEBUG_LOG_DIR,
                        configuration.getAggregationWorker().getDebugLogDir())) {
            return null;
        }
        return configuration.getAggregationWorker().getDebugLogDir();
    }

    private int getDebugLogFrequencyFromConfig(AgentsServiceConfiguration configuration) {
        if (Objects.isNull(configuration) || Objects.isNull(configuration.getAggregationWorker())) {
            return 0;
        }
        return configuration.getAggregationWorker().getDebugFrequencyPercent();
    }

    private String getLongTermStorageDisputeBasePrefixFromConfig(
            AgentsServiceConfiguration configuration) {
        if (Objects.isNull(configuration) || Objects.isNull(configuration.getAggregationWorker())) {
            return "";
        }
        return configuration.getAggregationWorker().getLongTermStorageDisputeBasePrefix();
    }

    public int getDebugFrequencyPercent() {
        return debugLogFrequencyPercent;
    }

    public String getLongTermStorageDisputeBasePrefixFromConfig() {
        return longTermStorageDisputeBasePrefix;
    }
}

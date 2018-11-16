package se.tink.backend.aggregation.workers.commands.state;

import java.util.Objects;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

import java.io.File;
import se.tink.backend.aggregation.configuration.AggregationWorkerConfiguration;

public class DebugAgentWorkerCommandState {
    private static final Logger log = LoggerFactory.getLogger(DebugAgentWorkerCommandState.class);
    private final String debugLogDir;
    private File debugDirectory;

    @Inject
    public DebugAgentWorkerCommandState(AgentsServiceConfiguration configuration) {
        if (Objects.isNull(configuration)) {
            this.debugLogDir = null;
            return;
        }

        if (Objects.isNull(configuration.getAggregationWorker())) {
            this.debugLogDir = null;
            return;
        }

        if (Objects.isNull(configuration.getAggregationWorker().getDebugLogDir())) {
            this.debugLogDir = null;
            return;
        }

        if (Objects.equals(
                AggregationWorkerConfiguration.DEFAULT_DEBUG_LOG_DIR,
                configuration.getAggregationWorker().getDebugLogDir())) {
            this.debugLogDir = null;
            return;
        }

        this.debugLogDir = configuration.getAggregationWorker().getDebugLogDir();
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
            throw new RuntimeException("Could not make sure log directory was created: "
                    + this.debugDirectory.getAbsolutePath(), e);
        }

        return this.debugDirectory;
    }
}

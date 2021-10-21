package se.tink.backend.aggregation.storage.logs.handlers;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AggregationWorkerConfiguration;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsStorageHandler;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.AgentDebugLogBucket;

@Slf4j
public class AgentHttpLogsLocalStorageHandler implements AgentHttpLogsStorageHandler {

    private final AgentsServiceConfiguration agentsServiceConfiguration;

    @Inject
    public AgentHttpLogsLocalStorageHandler(AgentsServiceConfiguration agentsServiceConfiguration) {
        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    @Override
    public boolean isEnabled() {
        if (agentsServiceConfiguration == null) {
            log.warn("Local storage disabled - missing configuration");
            return false;
        }
        return true;
    }

    @Override
    public String storeLog(String content, String filePath, AgentDebugLogBucket bucket)
            throws IOException {
        if (!isEnabled()) {
            throw new IllegalStateException("Invalid attempt to use local storage - not enabled");
        }

        Path targetFilePath = Paths.get(getDebugLogDirFromConfig(), filePath);
        Files.createDirectories(targetFilePath.getParent());

        Path savedFilePath = Files.write(targetFilePath, content.getBytes(StandardCharsets.UTF_8));
        return savedFilePath.toAbsolutePath().toString();
    }

    private String getDebugLogDirFromConfig() {
        return Optional.of(agentsServiceConfiguration.getAggregationWorker())
                .map(AggregationWorkerConfiguration::getDebugLogDir)
                .orElse(AggregationWorkerConfiguration.DEFAULT_DEBUG_LOG_DIR);
    }
}

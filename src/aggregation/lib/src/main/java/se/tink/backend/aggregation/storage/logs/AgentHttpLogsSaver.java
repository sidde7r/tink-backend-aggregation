package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.AapLogsCatalog;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.AgentDebugLogBucket;
import se.tink.backend.aggregation.storage.logs.handlers.S3StoragePathsProvider;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentHttpLogsSaver {

    private final AgentHttpLogsStorageHandler logsStorageHandler;
    private final AgentHttpLogsCache logsCache;
    private final S3StoragePathsProvider s3StoragePathsProvider;

    /**
     * Store AAP logs
     *
     * @param catalog - catalog where logs should be stored
     * @return path to saved file
     */
    public SaveLogsResult saveAapLogs(AapLogsCatalog catalog) {
        if (!logsStorageHandler.isEnabled()) {
            return SaveLogsResult.skipped(SaveLogsStatus.NO_AVAILABLE_STORAGE);
        }

        Optional<String> maybeLogContent = logsCache.getAapLogContent();
        if (!maybeLogContent.isPresent()) {
            return SaveLogsResult.skipped(SaveLogsStatus.NO_LOGS);
        }

        String logContent = maybeLogContent.get();
        if (StringUtils.isBlank(logContent)) {
            return SaveLogsResult.skipped(SaveLogsStatus.EMPTY_LOGS);
        }

        try {
            String filePath = getAapLogFilePath(logContent, catalog);
            String storageDescription =
                    logsStorageHandler.storeLog(
                            logContent, filePath, AgentDebugLogBucket.AAP_FORMAT_LOGS);
            return SaveLogsResult.saved(storageDescription);

        } catch (IOException | RuntimeException e) {
            log.error("Could not store AAP logs, catalog: {}", catalog);
            return SaveLogsResult.skipped(SaveLogsStatus.ERROR);
        }
    }

    private String getAapLogFilePath(String cleanLogContent, AapLogsCatalog catalog) {
        if (catalog == AapLogsCatalog.DEFAULT) {
            return s3StoragePathsProvider.getAapLogDefaultPath(cleanLogContent);
        }
        if (catalog == AapLogsCatalog.LTS_PAYMENTS) {
            return s3StoragePathsProvider.getAapLogsPaymentsLtsPath(cleanLogContent);
        }
        throw new IllegalStateException("Unknown aap logs catalog: " + catalog);
    }

    /**
     * Store JSON logs
     *
     * @return path to saved file
     */
    public SaveLogsResult saveJsonLogs() {
        if (!logsStorageHandler.isEnabled()) {
            return SaveLogsResult.skipped(SaveLogsStatus.NO_AVAILABLE_STORAGE);
        }

        Optional<String> maybeJsonLogContent = logsCache.getJsonLogContent();
        if (!maybeJsonLogContent.isPresent()) {
            return SaveLogsResult.skipped(SaveLogsStatus.NO_LOGS);
        }
        String jsonLogContent = maybeJsonLogContent.get();

        String filePath = s3StoragePathsProvider.getJsonLogPath(jsonLogContent);
        try {
            String storagePath =
                    logsStorageHandler.storeLog(
                            jsonLogContent, filePath, AgentDebugLogBucket.JSON_FORMAT_LOGS);
            return SaveLogsResult.saved(storagePath);

        } catch (IOException | RuntimeException e) {
            log.error("Could not store JSON logs", e);
            return SaveLogsResult.skipped(SaveLogsStatus.ERROR);
        }
    }
}

package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.HttpLogType;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.RawHttpLogsCatalog;
import se.tink.backend.aggregation.storage.logs.handlers.S3StoragePathsProvider;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentHttpLogsSaver {

    private final AgentHttpLogsStorageHandler logsStorageHandler;
    private final AgentHttpLogsCache logsCache;
    private final S3StoragePathsProvider s3StoragePathsProvider;

    /**
     * Store raw logs
     *
     * @param catalog - catalog where logs should be stored
     * @return path to saved file
     */
    public SaveLogsResult saveRawLogs(RawHttpLogsCatalog catalog) {
        if (!logsStorageHandler.isEnabled()) {
            return SaveLogsResult.of(SaveLogsStatus.STORAGE_DISABLED);
        }

        Optional<String> maybeLogContent = logsCache.getRawLogContent();
        if (!maybeLogContent.isPresent()) {
            return SaveLogsResult.of(SaveLogsStatus.NO_LOGS);
        }

        String logContent = maybeLogContent.get();
        if (StringUtils.isBlank(logContent)) {
            return SaveLogsResult.of(SaveLogsStatus.EMPTY_LOGS);
        }

        try {
            String filePath = getRawLogFilePath(logContent, catalog);
            String storageDescription =
                    logsStorageHandler.storeLog(logContent, filePath, HttpLogType.RAW_FORMAT);
            return SaveLogsResult.saved(storageDescription);

        } catch (IOException | RuntimeException e) {
            log.error("Could not store raw logs, catalog: {}", catalog);
            return SaveLogsResult.of(SaveLogsStatus.ERROR);
        }
    }

    private String getRawLogFilePath(String cleanLogContent, RawHttpLogsCatalog catalog) {
        if (catalog == RawHttpLogsCatalog.DEFAULT) {
            return s3StoragePathsProvider.getRawLogDefaultPath(cleanLogContent);
        }
        if (catalog == RawHttpLogsCatalog.LTS_PAYMENTS) {
            return s3StoragePathsProvider.getRawLogsPaymentsLtsPath(cleanLogContent);
        }
        throw new IllegalStateException("Unknown raw logs catalog: " + catalog);
    }

    /**
     * Store JSON logs
     *
     * @return path to saved file
     */
    public SaveLogsResult saveJsonLogs() {
        if (!logsStorageHandler.isEnabled()) {
            return SaveLogsResult.of(SaveLogsStatus.STORAGE_DISABLED);
        }

        Optional<String> maybeJsonLogContent = logsCache.getJsonLogContent();
        if (!maybeJsonLogContent.isPresent()) {
            return SaveLogsResult.of(SaveLogsStatus.NO_LOGS);
        }
        String jsonLogContent = maybeJsonLogContent.get();

        String filePath = s3StoragePathsProvider.getJsonLogPath(jsonLogContent);
        try {
            String storagePath =
                    logsStorageHandler.storeLog(jsonLogContent, filePath, HttpLogType.JSON_FORMAT);
            return SaveLogsResult.saved(storagePath);

        } catch (IOException | RuntimeException e) {
            log.error("Could not store JSON logs", e);
            return SaveLogsResult.of(SaveLogsStatus.ERROR);
        }
    }
}

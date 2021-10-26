package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.http.log.executor.HttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.HttpLogType;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.RawHttpLogsCatalog;
import se.tink.backend.aggregation.storage.logs.handlers.S3StoragePathsProvider;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentHttpLogsSaver {

    private final AgentHttpLogsStorageHandler logsStorageHandler;
    private final AgentHttpLogsCache logsCache;
    private final S3StoragePathsProvider s3StoragePathsProvider;

    private final RawHttpTrafficLogger rawHttpTrafficLogger;
    private final JsonHttpTrafficLogger jsonHttpTrafficLogger;

    /**
     * Store raw logs
     *
     * @param catalog - catalog where logs should be stored
     * @return path to saved file
     */
    public SaveLogsResult saveRawLogs(RawHttpLogsCatalog catalog) {
        Optional<SaveLogsStatus> maybeShouldNotSaveLogsStatus =
                checkForCommonReasonNotToSaveLogs(rawHttpTrafficLogger);
        if (maybeShouldNotSaveLogsStatus.isPresent()) {
            return SaveLogsResult.of(maybeShouldNotSaveLogsStatus.get());
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
            log.error("Could not store raw logs, catalog: {}", catalog, e);
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
        Optional<SaveLogsStatus> maybeShouldNotSaveLogsStatus =
                checkForCommonReasonNotToSaveLogs(jsonHttpTrafficLogger);
        if (maybeShouldNotSaveLogsStatus.isPresent()) {
            return SaveLogsResult.of(maybeShouldNotSaveLogsStatus.get());
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

    private Optional<SaveLogsStatus> checkForCommonReasonNotToSaveLogs(
            HttpTrafficLogger trafficLogger) {
        if (!logsStorageHandler.isEnabled()) {
            return Optional.of(SaveLogsStatus.STORAGE_DISABLED);
        }
        if (trafficLogger == null) {
            return Optional.of(SaveLogsStatus.NO_LOGGER);
        }
        if (shouldNotStoreLoggerTrafficInS3(trafficLogger)) {
            return Optional.of(SaveLogsStatus.LOGS_SHOULD_NOT_BE_STORED);
        }
        return Optional.empty();
    }

    private boolean shouldNotStoreLoggerTrafficInS3(HttpTrafficLogger trafficLogger) {
        boolean shouldStore =
                Optional.ofNullable(trafficLogger).map(HttpTrafficLogger::isEnabled).orElse(false);
        return !shouldStore;
    }
}

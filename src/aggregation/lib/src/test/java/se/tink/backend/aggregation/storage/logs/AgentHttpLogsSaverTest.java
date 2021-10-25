package se.tink.backend.aggregation.storage.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.HttpLogType;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.RawHttpLogsCatalog;
import se.tink.backend.aggregation.storage.logs.handlers.S3StoragePathsProvider;

@RunWith(JUnitParamsRunner.class)
public class AgentHttpLogsSaverTest {

    private AgentHttpLogsStorageHandler logsStorageHandler;
    private AgentHttpLogsCache logsCache;
    private S3StoragePathsProvider s3StoragePathsProvider;

    private AgentHttpLogsSaver logsSaver;

    @Before
    public void setup() {
        logsStorageHandler = mock(AgentHttpLogsStorageHandler.class);
        logsCache = mock(AgentHttpLogsCache.class);
        s3StoragePathsProvider = mock(S3StoragePathsProvider.class);

        logsSaver = new AgentHttpLogsSaver(logsStorageHandler, logsCache, s3StoragePathsProvider);
    }

    @Test
    public void should_skip_saving_any_logs_if_storage_handler_is_not_enabled() {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(false);

        // when
        List<SaveLogsResult> results =
                Stream.of(
                                logsSaver.saveRawLogs(RawHttpLogsCatalog.DEFAULT),
                                logsSaver.saveRawLogs(RawHttpLogsCatalog.LTS_PAYMENTS),
                                logsSaver.saveJsonLogs())
                        .collect(Collectors.toList());

        // then
        assertThat(results)
                .containsOnly(
                        SaveLogsResult.builder().status(SaveLogsStatus.STORAGE_DISABLED).build());

        verify(logsStorageHandler, times(3)).isEnabled();
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @Test
    @Parameters(method = "all_raw_logs_catalogs")
    public void should_skip_raw_logs_if_they_are_missing(RawHttpLogsCatalog logsCatalog) {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsCache.getRawLogContent()).thenReturn(Optional.empty());

        // when
        SaveLogsResult result = logsSaver.saveRawLogs(logsCatalog);

        // then
        assertThat(result)
                .isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.NO_LOGS).build());

        verify(logsCache).getRawLogContent();
        verify(logsStorageHandler).isEnabled();
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @SuppressWarnings("unused")
    private static Object[] all_raw_logs_catalogs() {
        return Stream.of(RawHttpLogsCatalog.values()).toArray();
    }

    @Test
    @Parameters(method = "all_raw_logs_catalogs")
    public void should_skip_raw_logs_if_they_are_empty(RawHttpLogsCatalog logsCatalog) {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsCache.getRawLogContent()).thenReturn(Optional.of("     "));

        // when
        SaveLogsResult result = logsSaver.saveRawLogs(logsCatalog);

        // then
        assertThat(result)
                .isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.EMPTY_LOGS).build());

        verify(logsCache).getRawLogContent();
        verify(logsStorageHandler).isEnabled();
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @Test
    @SneakyThrows
    public void should_save_not_empty_raw_logs_in_default_catalog() {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsStorageHandler.storeLog(any(), any(), any()))
                .thenReturn("HTTP: s3://bucket/raw/file.log");

        when(logsCache.getRawLogContent()).thenReturn(Optional.of("not empty log content"));
        when(s3StoragePathsProvider.getRawLogDefaultPath(any())).thenReturn("raw/file.log");

        // when
        SaveLogsResult result = logsSaver.saveRawLogs(RawHttpLogsCatalog.DEFAULT);

        // then
        assertThat(result)
                .isEqualTo(
                        SaveLogsResult.builder()
                                .status(SaveLogsStatus.SAVED)
                                .storageDescription("HTTP: s3://bucket/raw/file.log")
                                .build());

        verify(logsCache).getRawLogContent();
        verify(s3StoragePathsProvider).getRawLogDefaultPath("not empty log content");

        verify(logsStorageHandler).isEnabled();
        verify(logsStorageHandler)
                .storeLog("not empty log content", "raw/file.log", HttpLogType.RAW_FORMAT);
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @Test
    @SneakyThrows
    public void should_save_not_empty_raw_logs_in_payments_lts_catalog() {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsStorageHandler.storeLog(any(), any(), any()))
                .thenReturn("HTTP: s3://bucket/raw/lts/file.log");

        when(logsCache.getRawLogContent()).thenReturn(Optional.of("not empty log 123"));
        when(s3StoragePathsProvider.getRawLogsPaymentsLtsPath(any()))
                .thenReturn("raw/lts/file.log");

        // when
        SaveLogsResult result = logsSaver.saveRawLogs(RawHttpLogsCatalog.LTS_PAYMENTS);

        // then
        assertThat(result)
                .isEqualTo(
                        SaveLogsResult.builder()
                                .status(SaveLogsStatus.SAVED)
                                .storageDescription("HTTP: s3://bucket/raw/lts/file.log")
                                .build());

        verify(logsCache).getRawLogContent();
        verify(s3StoragePathsProvider).getRawLogsPaymentsLtsPath("not empty log 123");

        verify(logsStorageHandler).isEnabled();
        verify(logsStorageHandler)
                .storeLog("not empty log 123", "raw/lts/file.log", HttpLogType.RAW_FORMAT);
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @Test
    @SneakyThrows
    @Parameters(method = "all_raw_logs_catalogs")
    public void should_catch_exceptions_when_saving_raw_logs(RawHttpLogsCatalog logsCatalog) {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsStorageHandler.storeLog(any(), any(), any()))
                .thenThrow(new IOException("IO ERROR"));
        when(logsCache.getRawLogContent()).thenReturn(Optional.of("not empty log"));

        // when
        SaveLogsResult result = logsSaver.saveRawLogs(logsCatalog);

        // then
        assertThat(result).isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.ERROR).build());
    }

    @Test
    public void should_skip_json_logs_if_log_content_is_missing() {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsCache.getJsonLogContent()).thenReturn(Optional.empty());

        // when
        SaveLogsResult result = logsSaver.saveJsonLogs();

        // then
        assertThat(result)
                .isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.NO_LOGS).build());

        verify(logsCache).getJsonLogContent();

        verify(logsStorageHandler).isEnabled();
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @Test
    @SneakyThrows
    @Parameters(value = {"", "not empty json log content"})
    public void should_save_both_empty_and_not_empty_json_logs(String logContent) {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsStorageHandler.storeLog(any(), any(), any()))
                .thenReturn("HTTP: s3://bucket/ais/json/file.json");

        when(logsCache.getJsonLogContent()).thenReturn(Optional.of(logContent));

        when(s3StoragePathsProvider.getJsonLogPath(any())).thenReturn("ais/json/file.json");

        // when
        SaveLogsResult result = logsSaver.saveJsonLogs();

        // then
        assertThat(result)
                .isEqualTo(
                        SaveLogsResult.builder()
                                .status(SaveLogsStatus.SAVED)
                                .storageDescription("HTTP: s3://bucket/ais/json/file.json")
                                .build());

        verify(logsCache).getJsonLogContent();
        verify(s3StoragePathsProvider).getJsonLogPath(logContent);

        verify(logsStorageHandler).isEnabled();
        verify(logsStorageHandler)
                .storeLog(logContent, "ais/json/file.json", HttpLogType.JSON_FORMAT);
        verifyNoMoreInteractions(logsStorageHandler);
    }

    @Test
    @SneakyThrows
    public void should_catch_exceptions_when_saving_json_logs() {
        // given
        when(logsStorageHandler.isEnabled()).thenReturn(true);
        when(logsStorageHandler.storeLog(any(), any(), any()))
                .thenThrow(new IOException("IO ERROR"));

        when(logsCache.getJsonLogContent()).thenReturn(Optional.of("whatever"));

        // when
        SaveLogsResult result = logsSaver.saveJsonLogs();

        // then
        assertThat(result).isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.ERROR).build());
    }
}

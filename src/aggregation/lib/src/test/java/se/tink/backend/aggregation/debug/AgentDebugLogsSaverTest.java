package se.tink.backend.aggregation.debug;

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
import se.tink.backend.aggregation.storage.debug.AgentDebugLogStorageHandler;
import se.tink.backend.aggregation.storage.debug.AgentDebugLogsCache;
import se.tink.backend.aggregation.storage.debug.AgentDebugLogsSaver;
import se.tink.backend.aggregation.storage.debug.SaveLogsResult;
import se.tink.backend.aggregation.storage.debug.SaveLogsStatus;
import se.tink.backend.aggregation.storage.debug.handlers.AgentDebugLogConstants.AapLogsCatalog;
import se.tink.backend.aggregation.storage.debug.handlers.AgentDebugLogConstants.AgentDebugLogBucket;
import se.tink.backend.aggregation.storage.debug.handlers.S3StoragePathsProvider;

@RunWith(JUnitParamsRunner.class)
public class AgentDebugLogsSaverTest {

    private AgentDebugLogStorageHandler logStorageHandler;
    private AgentDebugLogsCache logsCachingProvider;
    private S3StoragePathsProvider s3StoragePathsProvider;

    private AgentDebugLogsSaver logsSaver;

    @Before
    public void setup() {
        logStorageHandler = mock(AgentDebugLogStorageHandler.class);
        logsCachingProvider = mock(AgentDebugLogsCache.class);
        s3StoragePathsProvider = mock(S3StoragePathsProvider.class);

        logsSaver =
                new AgentDebugLogsSaver(
                        logStorageHandler, logsCachingProvider, s3StoragePathsProvider);
    }

    @Test
    public void should_skip_saving_any_logs_if_storage_handler_is_not_enabled() {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(false);

        // when
        List<SaveLogsResult> results =
                Stream.of(
                                logsSaver.saveAapLogs(AapLogsCatalog.DEFAULT),
                                logsSaver.saveAapLogs(AapLogsCatalog.LTS_PAYMENTS),
                                logsSaver.saveJsonLogs())
                        .collect(Collectors.toList());

        // then
        assertThat(results)
                .containsOnly(
                        SaveLogsResult.builder()
                                .status(SaveLogsStatus.NO_AVAILABLE_STORAGE)
                                .build());

        verify(logStorageHandler, times(3)).isEnabled();
        verifyNoMoreInteractions(logStorageHandler);
    }

    @Test
    @Parameters(method = "all_aap_logs_catalogs")
    public void should_skip_aap_logs_if_they_are_missing(AapLogsCatalog logsCatalog) {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logsCachingProvider.getAapLogContent()).thenReturn(Optional.empty());

        // when
        SaveLogsResult result = logsSaver.saveAapLogs(logsCatalog);

        // then
        assertThat(result)
                .isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.NO_LOGS).build());

        verify(logsCachingProvider).getAapLogContent();
        verify(logStorageHandler).isEnabled();
        verifyNoMoreInteractions(logStorageHandler);
    }

    @SuppressWarnings("unused")
    private static Object[] all_aap_logs_catalogs() {
        return Stream.of(AapLogsCatalog.values()).toArray();
    }

    @Test
    @Parameters(method = "all_aap_logs_catalogs")
    public void should_skip_aap_logs_if_they_are_empty(AapLogsCatalog logsCatalog) {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logsCachingProvider.getAapLogContent()).thenReturn(Optional.of("     "));

        // when
        SaveLogsResult result = logsSaver.saveAapLogs(logsCatalog);

        // then
        assertThat(result)
                .isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.EMPTY_LOGS).build());

        verify(logsCachingProvider).getAapLogContent();
        verify(logStorageHandler).isEnabled();
        verifyNoMoreInteractions(logStorageHandler);
    }

    @Test
    @SneakyThrows
    public void should_save_not_empty_aap_logs_in_default_catalog() {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logStorageHandler.storeDebugLog(any(), any(), any()))
                .thenReturn("HTTP: s3://bucket/aap/file.log");

        when(logsCachingProvider.getAapLogContent())
                .thenReturn(Optional.of("not empty log content"));
        when(s3StoragePathsProvider.getAapLogDefaultPath(any())).thenReturn("aap/file.log");

        // when
        SaveLogsResult result = logsSaver.saveAapLogs(AapLogsCatalog.DEFAULT);

        // then
        assertThat(result)
                .isEqualTo(
                        SaveLogsResult.builder()
                                .status(SaveLogsStatus.SAVED)
                                .storageDescription("HTTP: s3://bucket/aap/file.log")
                                .build());

        verify(logsCachingProvider).getAapLogContent();
        verify(s3StoragePathsProvider).getAapLogDefaultPath("not empty log content");

        verify(logStorageHandler).isEnabled();
        verify(logStorageHandler)
                .storeDebugLog(
                        "not empty log content",
                        "aap/file.log",
                        AgentDebugLogBucket.AAP_FORMAT_LOGS);
        verifyNoMoreInteractions(logStorageHandler);
    }

    @Test
    @SneakyThrows
    public void should_save_not_empty_aap_logs_in_payments_lts_catalog() {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logStorageHandler.storeDebugLog(any(), any(), any()))
                .thenReturn("HTTP: s3://bucket/aap/lts/file.log");

        when(logsCachingProvider.getAapLogContent()).thenReturn(Optional.of("not empty log 123"));
        when(s3StoragePathsProvider.getAapLogsPaymentsLtsPath(any()))
                .thenReturn("aap/lts/file.log");

        // when
        SaveLogsResult result = logsSaver.saveAapLogs(AapLogsCatalog.LTS_PAYMENTS);

        // then
        assertThat(result)
                .isEqualTo(
                        SaveLogsResult.builder()
                                .status(SaveLogsStatus.SAVED)
                                .storageDescription("HTTP: s3://bucket/aap/lts/file.log")
                                .build());

        verify(logsCachingProvider).getAapLogContent();
        verify(s3StoragePathsProvider).getAapLogsPaymentsLtsPath("not empty log 123");

        verify(logStorageHandler).isEnabled();
        verify(logStorageHandler)
                .storeDebugLog(
                        "not empty log 123",
                        "aap/lts/file.log",
                        AgentDebugLogBucket.AAP_FORMAT_LOGS);
        verifyNoMoreInteractions(logStorageHandler);
    }

    @Test
    @SneakyThrows
    @Parameters(method = "all_aap_logs_catalogs")
    public void should_catch_exceptions_when_saving_aap_logs(AapLogsCatalog logsCatalog) {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logStorageHandler.storeDebugLog(any(), any(), any()))
                .thenThrow(new IOException("IO ERROR"));
        when(logsCachingProvider.getAapLogContent()).thenReturn(Optional.of("not empty log"));

        // when
        SaveLogsResult result = logsSaver.saveAapLogs(logsCatalog);

        // then
        assertThat(result).isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.ERROR).build());
    }

    @Test
    public void should_skip_json_logs_if_log_content_is_missing() {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logsCachingProvider.getJsonLogContent()).thenReturn(Optional.empty());

        // when
        SaveLogsResult result = logsSaver.saveJsonLogs();

        // then
        assertThat(result)
                .isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.NO_LOGS).build());

        verify(logsCachingProvider).getJsonLogContent();

        verify(logStorageHandler).isEnabled();
        verifyNoMoreInteractions(logStorageHandler);
    }

    @Test
    @SneakyThrows
    @Parameters(value = {"", "not empty json log content"})
    public void should_save_both_empty_and_not_empty_json_logs(String logContent) {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logStorageHandler.storeDebugLog(any(), any(), any()))
                .thenReturn("HTTP: s3://bucket/ais/json/file.json");

        when(logsCachingProvider.getJsonLogContent()).thenReturn(Optional.of(logContent));

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

        verify(logsCachingProvider).getJsonLogContent();
        verify(s3StoragePathsProvider).getJsonLogPath(logContent);

        verify(logStorageHandler).isEnabled();
        verify(logStorageHandler)
                .storeDebugLog(
                        logContent, "ais/json/file.json", AgentDebugLogBucket.JSON_FORMAT_LOGS);
        verifyNoMoreInteractions(logStorageHandler);
    }

    @Test
    @SneakyThrows
    public void should_catch_exceptions_when_saving_json_logs() {
        // given
        when(logStorageHandler.isEnabled()).thenReturn(true);
        when(logStorageHandler.storeDebugLog(any(), any(), any()))
                .thenThrow(new IOException("IO ERROR"));

        when(logsCachingProvider.getJsonLogContent()).thenReturn(Optional.of("whatever"));

        // when
        SaveLogsResult result = logsSaver.saveJsonLogs();

        // then
        assertThat(result).isEqualTo(SaveLogsResult.builder().status(SaveLogsStatus.ERROR).build());
    }
}

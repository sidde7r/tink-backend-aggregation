package se.tink.backend.aggregation.storage.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.libraries.se.tink.libraries.har_logger.src.logger.HarLogCollector;

public class AgentHttpLogsCacheTest {

    private AgentHttpLogsMasker httpLogsMasker;
    private RawHttpTrafficLogger rawHttpTrafficLogger;
    private JsonHttpTrafficLogger jsonHttpTrafficLogger;
    private HarLogCollector harLogCollector;

    private AgentHttpLogsCache httpLogsCache;

    @Before
    public void setup() {
        httpLogsMasker = mock(AgentHttpLogsMasker.class);
        rawHttpTrafficLogger = mock(RawHttpTrafficLogger.class);
        jsonHttpTrafficLogger = mock(JsonHttpTrafficLogger.class);
        harLogCollector = mock(HarLogCollector.class);

        httpLogsCache =
                new AgentHttpLogsCache(
                        httpLogsMasker,
                        rawHttpTrafficLogger,
                        jsonHttpTrafficLogger,
                        harLogCollector);
    }

    @Test
    public void should_return_and_cache_masked_raw_logs() {
        // given
        when(rawHttpTrafficLogger.tryGetLogContent()).thenReturn(Optional.of("raw log content"));
        when(httpLogsMasker.maskSensitiveOutputLog(any())).thenReturn("masked raw log content");

        // when
        Optional<String> log1 = httpLogsCache.getRawLogContent();
        Optional<String> log2 = httpLogsCache.getRawLogContent();
        Optional<String> log3 = httpLogsCache.getRawLogContent();

        // then
        assertThat(log1).hasValue("masked raw log content");
        assertThat(log2).hasValue("masked raw log content");
        assertThat(log3).hasValue("masked raw log content");

        verify(rawHttpTrafficLogger, times(1)).tryGetLogContent();
        verify(httpLogsMasker, times(1)).maskSensitiveOutputLog("raw log content");
    }

    @Test
    public void should_return_and_cache_empty_raw_logs() {
        // given
        when(rawHttpTrafficLogger.tryGetLogContent()).thenReturn(Optional.empty());

        // when
        Optional<String> log1 = httpLogsCache.getRawLogContent();
        Optional<String> log2 = httpLogsCache.getRawLogContent();
        Optional<String> log3 = httpLogsCache.getRawLogContent();

        // then
        assertThat(log1).isEmpty();
        assertThat(log2).isEmpty();
        assertThat(log3).isEmpty();

        verify(rawHttpTrafficLogger, times(1)).tryGetLogContent();
    }

    @Test
    public void should_return_and_cache_masked_json_logs() {
        // given
        when(jsonHttpTrafficLogger.tryGetLogContent()).thenReturn(Optional.of("json log content"));
        when(httpLogsMasker.maskSensitiveOutputLog(any())).thenReturn("masked json log content");

        // when
        Optional<String> log1 = httpLogsCache.getJsonLogContent();
        Optional<String> log2 = httpLogsCache.getJsonLogContent();
        Optional<String> log3 = httpLogsCache.getJsonLogContent();

        // then
        assertThat(log1).hasValue("masked json log content");
        assertThat(log2).hasValue("masked json log content");
        assertThat(log3).hasValue("masked json log content");

        verify(jsonHttpTrafficLogger, times(1)).tryGetLogContent();
        verify(httpLogsMasker, times(1)).maskSensitiveOutputLog("json log content");
    }

    @Test
    public void should_return_and_cache_empty_json_logs() {
        // given
        when(jsonHttpTrafficLogger.tryGetLogContent()).thenReturn(Optional.empty());

        // when
        Optional<String> log1 = httpLogsCache.getJsonLogContent();
        Optional<String> log2 = httpLogsCache.getJsonLogContent();
        Optional<String> log3 = httpLogsCache.getJsonLogContent();

        // then
        assertThat(log1).isEmpty();
        assertThat(log2).isEmpty();
        assertThat(log3).isEmpty();

        verify(jsonHttpTrafficLogger, times(1)).tryGetLogContent();
    }
}

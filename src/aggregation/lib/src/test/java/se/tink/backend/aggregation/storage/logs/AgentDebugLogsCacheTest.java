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
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLogger;

public class AgentDebugLogsCacheTest {

    private AgentDebugLogsMasker debugLogsMasker;
    private HttpAapLogger httpAapLogger;
    private HttpJsonLogger httpJsonLogger;

    private AgentDebugLogsCache logsCache;

    @Before
    public void setup() {
        debugLogsMasker = mock(AgentDebugLogsMasker.class);
        httpAapLogger = mock(HttpAapLogger.class);
        httpJsonLogger = mock(HttpJsonLogger.class);

        logsCache = new AgentDebugLogsCache(debugLogsMasker, httpAapLogger, httpJsonLogger);
    }

    @Test
    public void should_return_and_cache_masked_aap_logs() {
        // given
        when(httpAapLogger.tryGetLogContent()).thenReturn(Optional.of("raw aap log content"));
        when(debugLogsMasker.maskSensitiveOutputLog(any())).thenReturn("masked aap log content");

        // when
        Optional<String> log1 = logsCache.getAapLogContent();
        Optional<String> log2 = logsCache.getAapLogContent();
        Optional<String> log3 = logsCache.getAapLogContent();

        // then
        assertThat(log1).hasValue("masked aap log content");
        assertThat(log2).hasValue("masked aap log content");
        assertThat(log3).hasValue("masked aap log content");

        verify(httpAapLogger, times(1)).tryGetLogContent();
        verify(debugLogsMasker, times(1)).maskSensitiveOutputLog("raw aap log content");
    }

    @Test
    public void should_return_and_cache_empty_aap_logs() {
        // given
        when(httpAapLogger.tryGetLogContent()).thenReturn(Optional.empty());

        // when
        Optional<String> log1 = logsCache.getAapLogContent();
        Optional<String> log2 = logsCache.getAapLogContent();
        Optional<String> log3 = logsCache.getAapLogContent();

        // then
        assertThat(log1).isEmpty();
        assertThat(log2).isEmpty();
        assertThat(log3).isEmpty();

        verify(httpAapLogger, times(1)).tryGetLogContent();
    }

    @Test
    public void should_return_and_cache_masked_json_logs() {
        // given
        when(httpJsonLogger.tryGetLogContent()).thenReturn(Optional.of("raw json log content"));
        when(debugLogsMasker.maskSensitiveOutputLog(any())).thenReturn("masked json log content");

        // when
        Optional<String> log1 = logsCache.getJsonLogContent();
        Optional<String> log2 = logsCache.getJsonLogContent();
        Optional<String> log3 = logsCache.getJsonLogContent();

        // then
        assertThat(log1).hasValue("masked json log content");
        assertThat(log2).hasValue("masked json log content");
        assertThat(log3).hasValue("masked json log content");

        verify(httpJsonLogger, times(1)).tryGetLogContent();
        verify(debugLogsMasker, times(1)).maskSensitiveOutputLog("raw json log content");
    }

    @Test
    public void should_return_and_cache_empty_json_logs() {
        // given
        when(httpJsonLogger.tryGetLogContent()).thenReturn(Optional.empty());

        // when
        Optional<String> log1 = logsCache.getJsonLogContent();
        Optional<String> log2 = logsCache.getJsonLogContent();
        Optional<String> log3 = logsCache.getJsonLogContent();

        // then
        assertThat(log1).isEmpty();
        assertThat(log2).isEmpty();
        assertThat(log3).isEmpty();

        verify(httpJsonLogger, times(1)).tryGetLogContent();
    }
}

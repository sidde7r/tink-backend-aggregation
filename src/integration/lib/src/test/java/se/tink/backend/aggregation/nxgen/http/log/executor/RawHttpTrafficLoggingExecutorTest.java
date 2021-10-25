package se.tink.backend.aggregation.nxgen.http.log.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLoggingExecutor;

@RunWith(MockitoJUnitRunner.class)
public class RawHttpTrafficLoggingExecutorTest {

    private LoggingExecutor loggingExecutor;

    private RawHttpTrafficLogger rawHttpTrafficLogger;

    @Mock private LogMasker logMasker;

    @Before
    public void init() {
        rawHttpTrafficLogger =
                RawHttpTrafficLogger.inMemoryLogger()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not create in memory logger"));
        when(logMasker.mask(any()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0));
    }

    @Test
    public void shouldLogWhenLoggingMaskerCoversSecrets() {
        loggingExecutor =
                new RawHttpTrafficLoggingExecutor(
                        rawHttpTrafficLogger, logMasker, LoggingMode.LOGGING_MASKER_COVERS_SECRETS);

        loggingExecutor.log(exampleRequest());
        loggingExecutor.log(exampleResponse());

        Optional<String> result = rawHttpTrafficLogger.tryGetLogContent();

        assertThat(result).isPresent();
        String[] lines = result.get().split("\n");
        assertThat(lines[0]).isEqualTo("1 * Client out-bound request");
        assertThat(lines[1]).matches("1 \\* \\d{4}-\\d{2}-\\d{2}--\\d{2}:\\d{2}:\\d{2}.\\d{3}");
        assertThat(lines[2]).isEqualTo("1 > GET http://localhost/abc/def");
        assertThat(lines[3]).isEqualTo("1 > Authorization: ***");
        assertThat(lines[4]).isEqualTo("1 > User-Agent: Test!");
        assertThat(lines[5]).isEqualTo("1 > Something: ***");
        assertThat(lines[6]).isEqualTo("a body");
        assertThat(lines[7]).isEqualTo("1 * Client in-bound response");
        assertThat(lines[8]).matches("1 \\* \\d{4}-\\d{2}-\\d{2}--\\d{2}:\\d{2}:\\d{2}.\\d{3}");
        assertThat(lines[9]).isEqualTo("1 < 200");
        assertThat(lines[10]).isEqualTo("1 < Set-Cookie: ***");
        assertThat(lines[11]).isEqualTo("1 < ");
        assertThat(lines[12]).isEqualTo("response!");
    }

    @Test
    public void shouldNotLogWhenLoggingMaskerUnsure() {
        loggingExecutor =
                new RawHttpTrafficLoggingExecutor(
                        rawHttpTrafficLogger,
                        logMasker,
                        LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS);

        loggingExecutor.log(exampleRequest());
        loggingExecutor.log(exampleResponse());

        String result = rawHttpTrafficLogger.getLoggingOutputStream().toString();

        assertThat(result).isEmpty();
    }

    private RequestLogEntry exampleRequest() {
        return RequestLogEntry.builder()
                .method("GET")
                .url("http://localhost/abc/def")
                .headers(
                        headers(
                                "Authorization",
                                "Bearer xxx",
                                "Something",
                                "custom",
                                "User-Agent",
                                "Test!"))
                .body("a body")
                .build();
    }

    private ResponseLogEntry exampleResponse() {
        return ResponseLogEntry.builder()
                .status(200)
                .headers(headers("Set-Cookie", "abc"))
                .body("response!")
                .build();
    }

    private Map<String, String> headers(String... data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("You must provide an even number of arguments");
        }

        Map<String, String> toReturn = new HashMap<>();

        for (int i = 0; i < data.length; i += 2) {
            toReturn.put(data[i], data[i + 1]);
        }

        return toReturn;
    }
}

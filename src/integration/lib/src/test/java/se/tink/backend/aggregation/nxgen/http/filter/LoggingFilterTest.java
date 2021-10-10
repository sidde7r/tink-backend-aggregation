package se.tink.backend.aggregation.nxgen.http.filter;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;

public class LoggingFilterTest {

    @Rule public WireMockRule rule = new WireMockRule(8888);

    @Test
    public void whenSendingRequestWithFormBody_oneOfTheLogOutputLinesExactlyMatchesTheBody() {

        HttpAapLogger httpAapLogger =
                HttpAapLogger.inMemoryLogger()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not create in memory logger"));

        TinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                new LogMaskerImpl(), LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .setHttpAapLogger(httpAapLogger)
                        .build();

        client.request("http://127.0.0.1:8888/__admin").body("hoy").post();

        final List<String> lines =
                httpAapLogger
                        .tryGetLogContent()
                        .map(content -> content.split("\\r?\\n"))
                        .map(Arrays::asList)
                        .orElse(Collections.emptyList());

        // At the printing of the request body, there should exist one line with the exact text
        // "hoy" (without the quotes)
        assert lines.contains("hoy");
    }
}

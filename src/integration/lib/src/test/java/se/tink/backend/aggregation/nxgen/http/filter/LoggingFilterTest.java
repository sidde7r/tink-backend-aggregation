package se.tink.backend.aggregation.nxgen.http.filter;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.utils.masker.CredentialsStringMaskerBuilder;

public class LoggingFilterTest {

    @Rule public WireMockRule rule = new WireMockRule(8888);

    @Test
    public void whenSendingRequestWithFormBody_oneOfTheLogOutputLinesExactlyMatchesTheBody() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        TinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder()
                                        .addStringMaskerBuilder(
                                                new CredentialsStringMaskerBuilder(
                                                        new Credentials()))
                                        .build(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .setLogOutputStream(printStream)
                        .build();

        client.request("http://127.0.0.1:8888/__admin").body("hoy").post();

        final List<String> lines = Arrays.asList(outputStream.toString().split("\\r?\\n"));

        // At the printing of the request body, there should exist one line with the exact text
        // "hoy" (without the quotes)
        assert lines.contains("hoy");
    }
}

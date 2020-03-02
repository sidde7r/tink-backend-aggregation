package se.tink.backend.aggregation.agents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.libraries.pair.Pair;

public class AapFileParserTest {

    @Test
    public void testParser() throws Exception {

        // Construct the list of expected result
        final List<Pair<String, String>> expectedRequestHeaders =
                Arrays.asList(
                        new Pair<>("Accept", "application/json"),
                        new Pair<>("X-Aggregator", "Tink Testing"));

        final List<Pair<String, String>> expectedResponseHeaders =
                Arrays.asList(
                        new Pair<>("Content-Type", "application/json;charset=utf-8"),
                        new Pair<>(
                                "Cache-Control", "private, no-store, no-cache, must-revalidate"));

        List<Pair<HTTPRequest, HTTPResponse>> expectedResult =
                Arrays.asList(
                        new Pair<>(
                                new HTTPRequest.Builder(
                                                "POST",
                                                "/mobileone/msl/services/transactions/v1/getDetails",
                                                expectedRequestHeaders)
                                        .withRequestBody(
                                                "{\"billingIndexList\":[0],\"sortedIndex\":0}")
                                        .build(),
                                new HTTPResponse.Builder(expectedResponseHeaders, 200)
                                        .withResponseBody("{}")
                                        .withToState("STATE1")
                                        .build()),
                        new Pair<>(
                                new HTTPRequest.Builder(
                                                "POST",
                                                "/mobileone/msl/services/accountservicing/v1/extendSession",
                                                expectedRequestHeaders)
                                        .withExpectedState("STATE1")
                                        .build(),
                                new HTTPResponse.Builder(expectedResponseHeaders, 200)
                                        .withResponseBody("{\"extendSession\":{\"status\":0}}")
                                        .build()));

        final String fileContent =
                new ResourceFileReader()
                        .read(
                                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/resources/test-refresh-traffic.aap");

        AapFileParser parser = new AapFileParser(fileContent);

        List<Pair<HTTPRequest, HTTPResponse>> pairs = parser.parseRequestResponsePairs();
        assertThat(pairs).isEqualTo(expectedResult);
    }
}

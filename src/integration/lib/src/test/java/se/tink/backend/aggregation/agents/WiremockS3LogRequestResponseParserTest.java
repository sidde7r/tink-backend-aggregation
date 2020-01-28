package se.tink.backend.aggregation.agents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.utils.wiremock.WiremockS3LogRequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.libraries.pair.Pair;

public class WiremockS3LogRequestResponseParserTest {

    @Test
    public void testParser() throws Exception {

        WiremockS3LogRequestResponseParser parser =
                new WiremockS3LogRequestResponseParser(
                        String.format(
                                "%s/%s",
                                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/resources",
                                "wiremock_test1.txt"),
                        "https://global.americanexpress.com");

        // Construct the list of expected result
        final List<Pair<String, String>> expectedRequestHeaders =
                Arrays.asList(
                        new Pair[] {
                            new Pair<>("Accept", "application/json"),
                            new Pair<>("X-Aggregator", "Tink Testing")
                        });

        final List<Pair<String, String>> expectedResponseHeaders =
                Arrays.asList(
                        new Pair[] {
                            new Pair<>("Content-Type", "application/json;charset=utf-8"),
                            new Pair<>(
                                    "Cache-Control", "private, no-store, no-cache, must-revalidate")
                        });

        List<Pair<HTTPRequest, HTTPResponse>> expectedResult =
                Arrays.asList(
                        new Pair[] {
                            new Pair<>(
                                    new HTTPRequest(
                                            "POST",
                                            "/mobileone/msl/services/transactions/v1/getDetails",
                                            expectedRequestHeaders,
                                            "{\"billingIndexList\":[0],\"sortedIndex\":0}"),
                                    new HTTPResponse(expectedResponseHeaders, 200, "{}")),
                            new Pair<>(
                                    new HTTPRequest(
                                            "POST",
                                            "/mobileone/msl/services/accountservicing/v1/extendSession",
                                            expectedRequestHeaders),
                                    new HTTPResponse(
                                            expectedResponseHeaders,
                                            200,
                                            "{\"extendSession\":{\"status\":0}}")),
                        });

        List<Pair<HTTPRequest, HTTPResponse>> pairs = parser.parseRequestResponsePairs();
        assertThat(pairs).isEqualTo(expectedResult);
    }
}

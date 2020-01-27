package se.tink.backend.aggregation.agents;

import java.util.List;
import org.junit.Assert;
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

        List<Pair<HTTPRequest, HTTPResponse>> pairs = parser.parseRequestResponsePairs();

        Assert.assertEquals(2, pairs.size());

        // Check headers for all requests and responses
        for (int index = 0; index < 2; index++) {
            Assert.assertTrue(
                    pairs.get(index).first.getRequestHeaders().get(0).first.equals("Accept"));
            Assert.assertTrue(
                    pairs.get(index)
                            .first
                            .getRequestHeaders()
                            .get(0)
                            .second
                            .equals("application/json"));

            Assert.assertTrue(
                    pairs.get(index).first.getRequestHeaders().get(1).first.equals("X-Aggregator"));
            Assert.assertTrue(
                    pairs.get(index)
                            .first
                            .getRequestHeaders()
                            .get(1)
                            .second
                            .equals("Tink Testing"));

            Assert.assertTrue(
                    pairs.get(index)
                            .second
                            .getResponseHeaders()
                            .get(0)
                            .first
                            .equals("Content-Type"));
            Assert.assertTrue(
                    pairs.get(index)
                            .second
                            .getResponseHeaders()
                            .get(0)
                            .second
                            .equals("application/json;charset=utf-8"));

            Assert.assertTrue(
                    pairs.get(index)
                            .second
                            .getResponseHeaders()
                            .get(1)
                            .first
                            .equals("Cache-Control"));
            Assert.assertTrue(
                    pairs.get(index)
                            .second
                            .getResponseHeaders()
                            .get(1)
                            .second
                            .equals("private, no-store, no-cache, must-revalidate"));
        }

        // Check request URL and method for each request
        Assert.assertTrue(pairs.get(0).first.getMethod().equalsIgnoreCase("post"));
        Assert.assertTrue(pairs.get(1).first.getMethod().equalsIgnoreCase("post"));
        Assert.assertTrue(
                pairs.get(0)
                        .first
                        .getUrl()
                        .equals("/mobileone/msl/services/transactions/v1/getDetails"));
        Assert.assertTrue(
                pairs.get(1)
                        .first
                        .getUrl()
                        .equals("/mobileone/msl/services/accountservicing/v1/extendSession"));

        // Check status code for each response
        Assert.assertTrue(pairs.get(0).second.getStatusCode() == 200);
        Assert.assertTrue(pairs.get(1).second.getStatusCode() == 200);

        // Check request bodies for each request
        Assert.assertTrue(pairs.get(0).first.getRequestBody().isPresent());
        Assert.assertTrue(
                pairs.get(0)
                        .first
                        .getRequestBody()
                        .get()
                        .equals("{\"billingIndexList\":[0],\"sortedIndex\":0}"));

        Assert.assertTrue(!pairs.get(1).first.getRequestBody().isPresent());

        // Check response bodies for each response
        Assert.assertTrue(pairs.get(0).second.getResponseBody().isPresent());
        Assert.assertTrue(pairs.get(0).second.getResponseBody().get().equals("{}"));

        Assert.assertTrue(pairs.get(1).second.getResponseBody().isPresent());
        Assert.assertTrue(
                pairs.get(1)
                        .second
                        .getResponseBody()
                        .get()
                        .equals("{\"extendSession\":{\"status\":0}}"));
    }
}

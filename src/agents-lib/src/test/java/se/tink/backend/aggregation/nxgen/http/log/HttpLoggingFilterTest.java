package se.tink.backend.aggregation.log;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.Arrays;
import javax.ws.rs.core.MediaType;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.HttpLoggableExecutor;
import se.tink.backend.aggregation.nxgen.http.log.HttpLoggingFilter;
import se.tink.backend.aggregation.utils.StringMasker;
import se.tink.backend.agents.rpc.Credentials;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class HttpLoggingFilterTest {
    private static final int MOCK_SERVER_PORT = findFreePort();
    private static final String MOCK_URL = "http://localhost:" + MOCK_SERVER_PORT;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(MOCK_SERVER_PORT);

    @Test
    @Ignore
    public void testHttpLoggingFilterIntegration() throws IOException {
        // Setup wiremock to stub response for post, attach a custom stream to system out to get the logs
        // and create the filter
        stubWiremockHttpResponse(createResponse1());
        ByteArrayOutputStream out = attachSystemOutStream();
        HttpLoggingFilter httpLoggingFilter = new HttpLoggingFilter(
                createAggregationLogger(), createLogTag(), createMaskStub(), createLoggedClass());

        // Attach the instance to the client and make a request to the stubbed http service and get back the
        // recorded output stream that the logger has put on it
        ClientResponse response = attachFilterAndExecuteFakeRequest(httpLoggingFilter);
        String string = closeAndGetOutputString(out);

        // Make sure that we get all expected stuff in the log
        assertThatlogContainsExpectedAndMaskedInfo(string);

        // And make sure the response isn't modified after the log has recorded everything
        assertThatResponseIsUntouched(response);
    }

    private static void assertThatlogContainsExpectedAndMaskedInfo(String string) {
        assertThat(string).contains(" [main] INFO");
        assertThat(string).contains("log.HttpLoggingFilter");
        assertThat(string).contains("[userId:userid credentialsId:credid]");
        assertThat(string).contains("HTTP(<TEST-TAG>");
        assertThat(string).contains("Request@{1:0}=");
        assertThat(string).contains("Response@{1:0}=");
        assertThat(string).contains("\"agent\":\"se.tink.backend.aggregation.agents.HttpLoggableExecutor\"");
        assertThat(string).contains("\"timestamp\":\"");
        assertThat(string).contains("\"maskedBody\":null,\"maskedHeader\":\"***MAAAAASK***{\\\"testone\\\":[\\\"***MASKED***\\\"],\\\"Content-Type\\\":[\\\"text/html\\\"],\\\"Accept\\\":[\\\"application/json\\\"]}\",\"maskedMethod\":\"POST\",\"maskedUri\":\"***MAAAAASK***http://localhost:");
        assertThat(string).contains("\"entryType\":\"Request\"");
        assertThat(string).contains("\"maskedBody\":\"***MAAAAASK***{\\n\\\"key1\\\":\\\"test\\\",\\n\\t\\\"error\\\":\\\"random stuff\\\",\\\"key3\\\":\\\"name@test.se\\\"}\\n\",\"maskedHeader\":\"***MAAAAASK***{\\\"CanBeSensitive\\\":[\\\"***MASKED***\\\"],\\\"Transfer-Encoding\\\":[\\\"chunked\\\"],\\\"Content-Type\\\":[\\\"application/json\\\"],\\\"Server\\\":[\\\"Jetty(6.1.x)\\\"]}\",\"maskedMethod\":null,\"maskedLocation\":null,\"statusCode\":\"403\",\"statusInfo\":\"Forbidden\",\"entryType\":\"Response\"");
    }

    private static void assertThatResponseIsUntouched(ClientResponse response) {
        assertThat(response.getEntity(String.class)).isEqualTo("{\n\"key1\":\"test\",\n\t\"error\":\"random stuff\",\"key3\":\"name@test.se\"}");
        assertThat(response.getHeaders().getFirst("CanBeSensitive")).isEqualTo("ValueOfSensitiveKey");
    }

    @Test
    public void testHttpLogginFilterDoesNotModifyOverlyLargeResponse() throws IOException {
        // Setup wiremock to stub response for post, attach a custom stream to system out to get the logs
        // and create the filter
        stubWiremockHttpResponse(createResponseThatsIncrediblyLarge());
        ByteArrayOutputStream out = attachSystemOutStream();
        HttpLoggingFilter httpLoggingFilter = new HttpLoggingFilter(
                createAggregationLogger(), createLogTag(), createMaskStub(), createLoggedClass());

        // Attach the instance to the client and make a request to the stubbed http service and get back the
        // recorded output stream that the logger has put on it
        ClientResponse response = attachFilterAndExecuteFakeRequest(httpLoggingFilter);
        String logged = closeAndGetOutputString(out);

        assertThat(logged.length()).isGreaterThan(500000);
        assertThat(logged).contains("abbbbb");
        String responseEntity = response.getEntity(String.class);
        assertThat(responseEntity.length()).isEqualTo(500000);
        assertThat(responseEntity).endsWith("abbbbb");
    }

    @Test
    public void httpLogginFilterSplitsLogLinesInMultipleLinesWhenLargeResponse() throws IOException {
        // Setup wiremock to stub response for post, attach a custom stream to system out to get the logs
        // and create the filter
        stubWiremockHttpResponse(createResponseThatsIncrediblyLarge());
        ByteArrayOutputStream out = attachSystemOutStream();
        HttpLoggingFilter httpLoggingFilter = new HttpLoggingFilter(
                createAggregationLogger(), createLogTag(), createMaskStub(), createLoggedClass());

        // Attach the instance to the client and make a request to the stubbed http service and get back the
        // recorded output stream that the logger has put on it
        attachFilterAndExecuteFakeRequest(httpLoggingFilter);
        String logged = closeAndGetOutputString(out);

        assertThat(logged.length()).isGreaterThan(500000);
        assertThat(logged).contains("abbbbb");
        String[] logLines = logged.split("\n");
        assertThat(logLines.length).isGreaterThan(10); // Some magic number just to be sure the response is split up
    }

    @Test
    public void httpLogginFilterTagsEachLineUniquely() throws IOException {
        // Setup wiremock to stub response for post, attach a custom stream to system out to get the logs
        // and create the filter
        stubWiremockHttpResponse(createResponseThatsIncrediblyLarge());
        ByteArrayOutputStream out = attachSystemOutStream();
        HttpLoggingFilter httpLoggingFilter = new HttpLoggingFilter(
                createAggregationLogger(), createLogTag(), createMaskStub(), createLoggedClass());

        // Attach the instance to the client and make a request to the stubbed http service and get back the
        // recorded output stream that the logger has put on it
        attachFilterAndExecuteFakeRequest(httpLoggingFilter);
        String logged = closeAndGetOutputString(out);

        String[] logLines = logged.split("\n");
        // Start on 1, since the first line is the request
        for (int i = 1; i < logLines.length; i++) {
            int responseLineNumber = i - 1;
            assertThat(logLines[i]).matches(".*Response@\\{1:" + responseLineNumber + "\\}=.+");
        }
    }

    private String createResponseThatsIncrediblyLarge() {
        char[] chars = new char[500000];
        Arrays.fill(chars, 'a');

        for (int i = 500; i < chars.length; i += 1000) {
            chars[i - 2] = '\n';
            chars[i - 1] = '\r';
            chars[i] = '\t';
        }

        chars[chars.length - 5] = 'b';
        chars[chars.length - 4] = 'b';
        chars[chars.length - 3] = 'b';
        chars[chars.length - 2] = 'b';
        chars[chars.length - 1] = 'b';

        return new String(chars);
    }

    private static String createResponse1() {
        return "{\n\"key1\":\"test\",\n\t\"error\":\"random stuff\",\"key3\":\"name@test.se\"}";
    }

    private static ClientResponse attachFilterAndExecuteFakeRequest(HttpLoggingFilter httpLoggingFilter) {
        TestHttpClient testHttpClient = new TestHttpClient();
        testHttpClient.addFilter(httpLoggingFilter);

        WebResource.Builder accept = testHttpClient
                .resource(MOCK_URL + "/test")
                .header("testone", "value")
                .type(MediaType.TEXT_HTML_TYPE)
                .accept(MediaType.APPLICATION_JSON);

        return accept.post(ClientResponse.class);
    }

    private static String closeAndGetOutputString(ByteArrayOutputStream out) throws IOException {
        out.close();
        return new String(out.toByteArray());
    }

    private static void stubWiremockHttpResponse(String response) {
        stubFor(post(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withHeader("CanBeSensitive", "ValueOfSensitiveKey")
                        .withBody(response)));
    }

    private static String createLogTag() {
        return "<TEST-TAG>";
    }

    private static Class<HttpLoggableExecutor> createLoggedClass() {
        return HttpLoggableExecutor.class;
    }

    private static AggregationLogger createAggregationLogger() {
        return new AggregationLogger(HttpLoggingFilter.class);
    }

    private static ByteArrayOutputStream attachSystemOutStream() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        System.setOut(printStream);
        return out;
    }

    private static Credentials createCredentialsStub() {
        Credentials credentials = new Credentials();
        credentials.setId("credid");
        credentials.setUserId("userid");
        credentials.setUsername("name@test.se");
        credentials.setPassword("mypasss");
        credentials.setSensitivePayload("test", "afafafa");
        return credentials;
    }

    private static ImmutableList<StringMasker> createMaskStub() {
        return ImmutableList.<StringMasker>of(string -> "***MAAAAASK***" + string);
    }

    private static int findFreePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int result = socket.getLocalPort();
            socket.close();

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class TestHttpClient extends Client {
    }
}

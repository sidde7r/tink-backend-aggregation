package se.tink.backend.aggregation.agents.framework.wiremock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.CompareEntity;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.MapComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.PlainTextComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.IntegrationWireMockTestTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class WireMockErrorDetectorTest {

    private static IntegrationWireMockTestTinkHttpClient httpClient;
    private static ObjectMapper mapper = new ObjectMapper();
    private static WireMockTestServer server;

    @BeforeClass
    public static void setup() {
        // given
        NextGenTinkHttpClient nextGenhttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        server =
                new WireMockTestServer(
                        ImmutableSet.of(
                                new AapFileParser(
                                        new ResourceFileReader()
                                                .read(
                                                        "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/resources/test.aap"))));

        httpClient =
                new IntegrationWireMockTestTinkHttpClient(
                        nextGenhttpClient, "localhost:" + server.getHttpPort());
    }

    @Before
    public void resetRequests() {
        // given
        server.resetRequests();
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectURLMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        Map<String, String> body = new HashMap<>();
        body.put("key1", "value1");
        body.put("key2", "value2");

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/wrong_endpoint")
                                        .header("Header1", "HeaderValue1")
                                        .header("Header2", "HeaderValue2")
                                        .type(MediaType.APPLICATION_JSON_TYPE)
                                        .post(String.class, mapper.writeValueAsString(body)));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        // and
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isFalse();
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectMethodMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/json")
                                        .header("Header1", "HeaderValue1")
                                        .header("Header2", "HeaderValue2")
                                        .type(MediaType.APPLICATION_JSON_TYPE)
                                        .get(String.class));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        // and
        assertThat(differences.areMethodsMatching()).isFalse();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).isEmpty();
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectHeaderMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        Map<String, String> body = new HashMap<>();
        body.put("key1", "value1");
        body.put("key2", "value2");

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/json")
                                        .header("Header1", "WrongValue")
                                        .type(MediaType.APPLICATION_JSON_TYPE)
                                        .post(String.class, mapper.writeValueAsString(body)));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        // and
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).containsOnly("header2");
        assertThat(differences.getHeaderKeysWithDifferentValues()).containsOnly("header1");
        // and
        MapComparisonReporter reporter = getReporter(differences, MapComparisonReporter.class);
        assertThat(reporter.getMissingBodyKeysInGivenRequest()).isEmpty();
        assertThat(reporter.getBodyKeysWithDifferentValue()).isEmpty();
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        Map<String, String> body = new HashMap<>();
        body.put("key1", "wrongValue1");

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/json")
                                        .header("Header1", "HeaderValue1")
                                        .header("Header2", "HeaderValue2")
                                        .type(MediaType.APPLICATION_JSON_TYPE)
                                        .post(String.class, mapper.writeValueAsString(body)));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).isEmpty();
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        MapComparisonReporter reporter = getReporter(differences, MapComparisonReporter.class);
        assertThat(reporter.getMissingBodyKeysInGivenRequest()).containsOnly("key2");
        assertThat(reporter.getBodyKeysWithDifferentValue()).containsOnly("key1");
    }

    @Test
    public void
            whenFormRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        String form = Form.builder().put("key1", "wrong_value").build().serialize();

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/urlencoded")
                                        .header("Header1", "HeaderValue1")
                                        .header("Header2", "HeaderValue2")
                                        .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                                        .post(String.class));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).isEmpty();
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        MapComparisonReporter reporter = getReporter(differences, MapComparisonReporter.class);
        assertThat(reporter.getMissingBodyKeysInGivenRequest()).containsOnly("key2");
        assertThat(reporter.getBodyKeysWithDifferentValue()).containsOnly("key1");
    }

    @Test
    public void whenSuccessfulXMLRequestIsMadeErrorDetectorShouldDetectNothing() {
        // given
        String body =
                "<v:Envelope xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "  <v:Header />\n"
                        + "  <v:Body>\n"
                        + "  <n0:authenticateCredential xmlns:n0=\"http://www.isban.es/webservices/TECHNICAL_FACADES/Security/F_facseg_security/internet/loginServicesNSegSAN/v1\" facade=\"loginServicesNSegSAN\">\n"
                        + "      <CB_AuthenticationData i:type=\":CB_AuthenticationData\">\n"
                        + "        <documento i:type=\":documento\">\n"
                        + "  <CODIGO_DOCUM_PERSONA_CORP i:type=\"d:string\">12345678</CODIGO_DOCUM_PERSONA_CORP>\n"
                        + "          <TIPO_DOCUM_PERSONA_CORP i:type=\"d:string\">N</TIPO_DOCUM_PERSONA_CORP>\n"
                        + "        </documento>\n"
                        + "        <password i:type=\"d:string\">hunter2</password>\n"
                        + "      </CB_AuthenticationData>\n"
                        + "      <userAddress i:type=\"d:string\">127.0.0.1</userAddress>\n"
                        + "    </n0:authenticateCredential>\n"
                        + "  </v:Body>\n"
                        + "</v:Envelope>";

        // when
        httpClient
                .request("http://dummy.com/xml_endpoint")
                .header("SOAPAction", "SoapAction")
                .type(MediaType.TEXT_XML_TYPE)
                .accept(MediaType.WILDCARD)
                .post(String.class, body);

        // then
        assertThat(server.hadEncounteredAnError()).isFalse();
    }

    @Test
    public void
            whenFailedXMLRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        String body =
                "<v:Envelope xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "  <v:Header />\n"
                        + "  <v:Body>\n"
                        + "    <n0:authenticateCredential xmlns:n0=\"http://www.isban.es/webservices/TECHNICAL_FACADES/Security/F_facseg_security/internet/loginServicesNSegSAN/v1\" facade=\"loginServicesNSegSAN\">\n"
                        + "      <CB_AuthenticationData i:type=\":CB_AuthenticationData\">\n"
                        + "        <documento i:type=\":documento\">\n"
                        + "          <TIPO_DOCUM_PERSONA_CORP i:type=\"d:string\">N</TIPO_DOCUM_PERSONA_CORP>\n"
                        + "        </documento>\n"
                        + "        <password i:type=\"d:string\">hunter2</password>\n"
                        + "      </CB_AuthenticationData>\n"
                        + "      <userAddress i:type=\"d:string\">127.0.0.1</userAddress>\n"
                        + "    </n0:authenticateCredential>\n"
                        + "  </v:Body>\n"
                        + "</v:Envelope>";

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/xml_endpoint")
                                        .header("SOAPAction", "SoapAction")
                                        .type(MediaType.TEXT_XML_TYPE)
                                        .accept(MediaType.WILDCARD)
                                        .post(String.class, body));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).isEmpty();
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        PlainTextComparisonReporter reporter =
                getReporter(differences, PlainTextComparisonReporter.class);
        assertThat(reporter.isThereDifference()).isTrue();
    }

    @Test
    public void
            whenFailedXMLRequestIsMadeErrorDetectorShouldDetectHeaderMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        String body =
                "<v:Envelope xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                        + "  <v:Header />\n"
                        + "  <v:Body>\n"
                        + "    <n0:authenticateCredential xmlns:n0=\"http://www.isban.es/webservices/TECHNICAL_FACADES/Security/F_facseg_security/internet/loginServicesNSegSAN/v1\" facade=\"loginServicesNSegSAN\">\n"
                        + "      <CB_AuthenticationData i:type=\":CB_AuthenticationData\">\n"
                        + "        <documento i:type=\":documento\">\n"
                        + "          <CODIGO_DOCUM_PERSONA_CORP i:type=\"d:string\">12345678</CODIGO_DOCUM_PERSONA_CORP>\n"
                        + "          <TIPO_DOCUM_PERSONA_CORP i:type=\"d:string\">N</TIPO_DOCUM_PERSONA_CORP>\n"
                        + "        </documento>\n"
                        + "        <password i:type=\"d:string\">hunter2</password>\n"
                        + "      </CB_AuthenticationData>\n"
                        + "      <userAddress i:type=\"d:string\">127.0.0.1</userAddress>\n"
                        + "    </n0:authenticateCredential>\n"
                        + "  </v:Body>\n"
                        + "</v:Envelope>";

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/xml_endpoint")
                                        .type(MediaType.TEXT_XML_TYPE)
                                        .accept(MediaType.WILDCARD)
                                        .post(String.class, body));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).containsOnly("soapaction");
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        PlainTextComparisonReporter reporter =
                getReporter(differences, PlainTextComparisonReporter.class);
        assertThat(reporter.isThereDifference()).isFalse();
    }

    @Test
    public void whenSuccessfulPlainTextRequestIsMadeErrorDetectorShouldDetectNothing() {
        // given
        String body = "this request body must be sent";

        // when
        httpClient
                .request("http://dummy.com/plaintext")
                .header("Header1", "HeaderValue1")
                .type(MediaType.TEXT_PLAIN_TYPE)
                .post(String.class, body);

        // then
        assertThat(server.hadEncounteredAnError()).isFalse();
    }

    @Test
    public void
            whenFailedPlainTextRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        String body = "wrong body";

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/plaintext")
                                        .header("Header1", "HeaderValue1")
                                        .type(MediaType.TEXT_PLAIN_TYPE)
                                        .post(String.class, body));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).isEmpty();
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        PlainTextComparisonReporter reporter =
                getReporter(differences, PlainTextComparisonReporter.class);
        assertThat(reporter.isThereDifference()).isTrue();
    }

    @Test
    public void
            whenFailedApplicationXmlRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        String body = "<Different></Different>";

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/applicationxml")
                                        .header("Header1", "HeaderValue1")
                                        .type(MediaType.APPLICATION_XML_TYPE)
                                        .post(String.class, body));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).isEmpty();
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        PlainTextComparisonReporter reporter =
                getReporter(differences, PlainTextComparisonReporter.class);
        assertThat(reporter.isThereDifference()).isTrue();
    }

    @Test
    public void
            whenFailedPlainTextRequestIsMadeErrorDetectorShouldDetectHeaderMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        String body = "this request body must be sent";

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                httpClient
                                        .request("http://dummy.com/plaintext")
                                        .type(MediaType.TEXT_PLAIN_TYPE)
                                        .post(String.class, body));

        // then
        assertException(throwable);
        // and
        CompareEntity differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        assertThat(differences.areMethodsMatching()).isTrue();
        assertThat(differences.areUrlsMatching()).isTrue();
        assertThat(differences.getMissingHeaderKeysInGivenRequest()).containsOnly("header1");
        assertThat(differences.getHeaderKeysWithDifferentValues()).isEmpty();
        // and
        PlainTextComparisonReporter reporter =
                getReporter(differences, PlainTextComparisonReporter.class);
        assertThat(reporter.isThereDifference()).isFalse();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getReporter(CompareEntity differences, Class<T> tClass) {
        assertThat(differences.getBodyComparisonReporter()).isInstanceOf(tClass);
        return (T) differences.getBodyComparisonReporter();
    }

    private static void assertException(Throwable throwable) {
        assertThat(throwable).isInstanceOf(HttpResponseException.class);
        assertThat(((HttpResponseException) throwable).getResponse().getStatus()).isEqualTo(404);
    }
}

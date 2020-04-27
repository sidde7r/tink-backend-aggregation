package se.tink.backend.aggregation.agents.framework.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.CompareEntity;
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

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/wrong_endpoint")
                            .header("Header1", "HeaderValue1")
                            .header("Header2", "HeaderValue2")
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(String.class, mapper.writeValueAsString(body));
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertTrue(differences.areMethodsMatching());
        Assert.assertEquals(0, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertEquals(0, differences.getMissingBodyKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getBodyKeysWithDifferentValues().size());
        Assert.assertFalse(differences.areUrlsMatching());
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectMethodMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        Map<String, String> body = new HashMap<>();
        body.put("key1", "value1");
        body.put("key2", "value2");

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/json")
                            .header("Header1", "HeaderValue1")
                            .header("Header2", "HeaderValue2")
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .get(String.class);
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertFalse(differences.areMethodsMatching());
        Assert.assertEquals(0, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertTrue(differences.areUrlsMatching());
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectHeaderMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        Map<String, String> body = new HashMap<>();
        body.put("key1", "value1");
        body.put("key2", "value2");

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/json")
                            .header("Header1", "WrongValue")
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(String.class, mapper.writeValueAsString(body));
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertTrue(differences.areMethodsMatching());
        Assert.assertTrue(differences.areUrlsMatching());
        Assert.assertEquals(0, differences.getMissingBodyKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getBodyKeysWithDifferentValues().size());
        Assert.assertEquals(1, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(1, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertTrue(differences.getMissingHeaderKeysInGivenRequest().contains("header2"));
        Assert.assertTrue(differences.getHeaderKeysWithDifferentValues().contains("header1"));
    }

    @Test
    public void
            whenFailedJSONRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        // given
        Map<String, String> body = new HashMap<>();
        body.put("key1", "wrongValue1");

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/json")
                            .header("Header1", "HeaderValue1")
                            .header("Header2", "HeaderValue2")
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .post(String.class, mapper.writeValueAsString(body));
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertTrue(differences.areMethodsMatching());
        Assert.assertTrue(differences.areUrlsMatching());
        Assert.assertEquals(0, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertEquals(1, differences.getMissingBodyKeysInGivenRequest().size());
        Assert.assertEquals(1, differences.getBodyKeysWithDifferentValues().size());
        Assert.assertTrue(differences.getMissingBodyKeysInGivenRequest().contains("key2"));
        Assert.assertTrue(differences.getBodyKeysWithDifferentValues().contains("key1"));
    }

    @Test
    public void
            whenFormRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {
        String form = Form.builder().put("key1", "wrong_value").build().serialize();

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/urlencoded")
                            .header("Header1", "HeaderValue1")
                            .header("Header2", "HeaderValue2")
                            .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                            .post(String.class);
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertTrue(differences.areMethodsMatching());
        Assert.assertTrue(differences.areUrlsMatching());
        Assert.assertEquals(0, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertEquals(1, differences.getMissingBodyKeysInGivenRequest().size());
        Assert.assertEquals(1, differences.getBodyKeysWithDifferentValues().size());
        Assert.assertTrue(differences.getMissingBodyKeysInGivenRequest().contains("key2"));
        Assert.assertTrue(differences.getBodyKeysWithDifferentValues().contains("key1"));
    }

    @Test
    public void whenSuccessfulXMLRequestIsMadeErrorDetectorShouldDetectNothing()
            throws IOException {

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

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/xml_endpoint")
                            .header("SOAPAction", "SoapAction")
                            .type(MediaType.TEXT_XML_TYPE)
                            .accept(MediaType.WILDCARD)
                            .post(String.class, body);
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNull(differences);
    }

    @Test
    public void
            whenFailedXMLRequestIsMadeErrorDetectorShouldDetectRequestBodyMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {

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

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/xml_endpoint")
                            .header("SOAPAction", "SoapAction")
                            .type(MediaType.TEXT_XML_TYPE)
                            .accept(MediaType.WILDCARD)
                            .post(String.class, body);
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertTrue(differences.areMethodsMatching());
        Assert.assertTrue(differences.areUrlsMatching());
        Assert.assertEquals(0, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertEquals(0, differences.getMissingBodyKeysInGivenRequest().size());
        Assert.assertEquals(1, differences.getBodyKeysWithDifferentValues().size());
        Assert.assertTrue(
                differences
                        .getBodyKeysWithDifferentValues()
                        .contains("Request bodies are different"));
    }

    @Test
    public void
            whenFailedXMLRequestIsMadeErrorDetectorShouldDetectHeaderMismatchBetweenFailedRequestAndClosestMatch()
                    throws IOException {

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

        CompareEntity differences = null;

        // when
        try {
            String response =
                    httpClient
                            .request("http://dummy.com/xml_endpoint")
                            .type(MediaType.TEXT_XML_TYPE)
                            .accept(MediaType.WILDCARD)
                            .post(String.class, body);
        } catch (HttpResponseException e) {
            differences = server.findDifferencesBetweenFailedRequestAndItsClosestMatch();
        }

        // then
        Assert.assertNotNull(differences);
        Assert.assertTrue(differences.areMethodsMatching());
        Assert.assertTrue(differences.areUrlsMatching());
        Assert.assertEquals(0, differences.getMissingBodyKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getBodyKeysWithDifferentValues().size());
        Assert.assertEquals(1, differences.getMissingHeaderKeysInGivenRequest().size());
        Assert.assertEquals(0, differences.getHeaderKeysWithDifferentValues().size());
        Assert.assertTrue(differences.getMissingHeaderKeysInGivenRequest().contains("soapaction"));
    }
}

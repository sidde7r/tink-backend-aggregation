package se.tink.backend.aggregation.agents;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPRequest.Builder;
import se.tink.backend.aggregation.agents.framework.wiremock.entities.HTTPResponse;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.libraries.pair.Pair;

public class AapFileParserTest {

    @Test
    public void testParser() {

        // Construct the set of expected result
        final ImmutableSet<Pair<String, String>> expectedRequestHeaders =
                ImmutableSet.of(
                        new Pair<>("Accept", "application/json"),
                        new Pair<>("X-Aggregator", "Tink Testing"));

        final ImmutableSet<Pair<String, String>> expectedResponseHeaders =
                ImmutableSet.of(
                        new Pair<>("Content-Type", "application/json;charset=utf-8"),
                        new Pair<>(
                                "Cache-Control", "private, no-store, no-cache, must-revalidate"));

        ImmutableSet<Pair<HTTPRequest, HTTPResponse>> expectedResult =
                ImmutableSet.of(
                        new Pair<>(
                                new HTTPRequest.Builder(
                                                "POST",
                                                "/mobileone/msl/services/transactions/v1/getDetails",
                                                expectedRequestHeaders)
                                        .setRequestBody(
                                                "{\"billingIndexList\":[0],\"sortedIndex\":0}")
                                        .build(),
                                new HTTPResponse.Builder(expectedResponseHeaders, 200)
                                        .setResponseBody("{}")
                                        .setToState("STATE1")
                                        .build()),
                        new Pair<>(
                                new HTTPRequest.Builder(
                                                "POST",
                                                "/mobileone/msl/services/accountservicing/v1/extendSession",
                                                expectedRequestHeaders)
                                        .setExpectedState("STATE1")
                                        .build(),
                                new HTTPResponse.Builder(expectedResponseHeaders, 200)
                                        .setResponseBody("{\"extendSession\":{\"status\":0}}")
                                        .build()),
                        new Pair<>(
                                new HTTPRequest.Builder(
                                                "POST",
                                                "/SANMOV_IPAD_NSeg_ENS/ws/SANMOV_Def_Listener",
                                                ImmutableSet.of(
                                                        new Pair<>(
                                                                "SOAPAction",
                                                                "https://www.bsan.mobi/SANMOV_IPAD_NSeg_ENS/ws/SANMOV_Def_Listener"),
                                                        new Pair<>(
                                                                "Content-Type",
                                                                "text/xml; charset=utf-8")))
                                        .setRequestBody(
                                                "<v:Envelope xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><v:Header /><v:Body><n0:authenticateCredential xmlns:n0=\"http://www.isban.es/webservices/TECHNICAL_FACADES/Security/F_facseg_security/internet/loginServicesNSegSAN/v1\" facade=\"loginServicesNSegSAN\"><CB_AuthenticationData i:type=\":CB_AuthenticationData\"><documento i:type=\":documento\"><CODIGO_DOCUM_PERSONA_CORP i:type=\"d:string\">12345678</CODIGO_DOCUM_PERSONA_CORP><TIPO_DOCUM_PERSONA_CORP i:type=\"d:string\">N</TIPO_DOCUM_PERSONA_CORP></documento><password i:type=\"d:string\">hunter2</password></CB_AuthenticationData><userAddress i:type=\"d:string\">127.0.0.1</userAddress></n0:authenticateCredential></v:Body></v:Envelope>")
                                        .build(),
                                new HTTPResponse.Builder(
                                                ImmutableSet.of(
                                                        new Pair<>(
                                                                "Content-Type",
                                                                "text/xml;charset=UTF-8")),
                                                200)
                                        .setResponseBody(
                                                "<soap-env:Envelope xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                                                        + "<soap-env:Header/>\n"
                                                        + "<soap-env:Body>\n"
                                                        + "<prefixRigel0:authenticateCredentialResponse xmlns:prefixRigel0=\"http://www.isban.es/webservices/SBAMOV/Services_la/F_sbamov_services_la/internet/loginServicesNew/v1\">\n"
                                                        + "<methodResult>\n"
                                                        + "<tokenCredential>aaaaaaaa-aaaa-4000-aaaa-aaaaaaaaaaaa</tokenCredential>\n"
                                                        + "</methodResult>\n"
                                                        + "</prefixRigel0:authenticateCredentialResponse>\n"
                                                        + "</soap-env:Body>\n"
                                                        + "</soap-env:Envelope>")
                                        .build()),
                        new Pair<>(
                                new Builder(
                                                "GET",
                                                "https://global.americanexpress.com/mobileone/msl/services/accountservicing/v1/list",
                                                expectedRequestHeaders)
                                        .build(),
                                HTTPResponse.faulty("MALFORMED_RESPONSE_CHUNK")));

        final String fileContent =
                ResourceFileReader.read(
                        "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/resources/test-refresh-traffic.aap");

        AapFileParser parser = new AapFileParser(fileContent);

        ImmutableSet<Pair<HTTPRequest, HTTPResponse>> pairs = parser.parseRequestResponsePairs();
        assertThat(pairs).isEqualTo(expectedResult);
    }
}

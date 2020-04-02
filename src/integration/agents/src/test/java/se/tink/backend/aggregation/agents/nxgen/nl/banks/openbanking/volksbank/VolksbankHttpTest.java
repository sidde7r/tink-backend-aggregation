package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.utils.masker.CredentialsStringMaskerBuilder;

public class VolksbankHttpTest {

    @Rule public WireMockRule rule = new WireMockRule(8888);

    @Test
    public void test() {
        stubFor(
                get(urlEqualTo("/my/resource"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/xml")
                                        .withBody("<response>Some content</response>")));

        TinkHttpClient client =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder()
                                        .addStringMaskerBuilder(
                                                new CredentialsStringMaskerBuilder(
                                                        new Credentials()))
                                        .build(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .build();

        String adminResponse = client.request("http://127.0.0.1:8888/__admin").get(String.class);

        System.out.println(adminResponse);

        String response = client.request("http://127.0.0.1:8888/my/resource").get(String.class);

        System.out.println(response);
    }
}

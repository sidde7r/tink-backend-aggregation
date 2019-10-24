package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.utils.Base64Masker;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

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
                                LogMasker.builder()
                                        .addStringMaskerBuilder(
                                                new CredentialsStringMaskerBuilder(
                                                        new Credentials(),
                                                        ImmutableList.of(
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .PASSWORD,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .SECRET_KEY,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .SENSITIVE_PAYLOAD,
                                                                CredentialsStringMaskerBuilder
                                                                        .CredentialsProperty
                                                                        .USERNAME)))
                                        .addStringMaskerBuilder(
                                                new ClientConfigurationStringMaskerBuilder(
                                                        Collections.emptyList()))
                                        .addStringMaskerBuilder(
                                                new Base64Masker(Collections.emptyList()))
                                        .build(),
                                LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .build();

        String adminResponse = client.request("http://127.0.0.1:8888/__admin").get(String.class);

        System.out.println(adminResponse);

        String response = client.request("http://127.0.0.1:8888/my/resource").get(String.class);

        System.out.println(response);
    }
}

package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.Base64Masker;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

public final class VolksbankApiClientMockTest {

    @Rule public WireMockRule rule = new WireMockRule(wireMockConfig().httpsPort(8888));

    private static TinkHttpClient createWiremockHttpClient() {
        TinkHttpClient tinkHttpClient =
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
        tinkHttpClient.setDebugOutput(true);
        tinkHttpClient.setCensorSensitiveHeaders(false);
        tinkHttpClient.disableSslVerification();
        return tinkHttpClient;
    }

    @Test
    public void wiretest() throws JSONException {
        final JSONObject scaOauth =
                new JSONObject()
                        .put("href", "https://psd.bancairediensten.nl/psd2/snsbank/v1/authorize");
        final JSONObject expectedBody =
                new JSONObject()
                        .put("consentStatus", "received")
                        .put("consentId", "SNS7777777777777")
                        .put("_links", new JSONObject().put("scaOAuth", scaOauth));

        stubFor(
                post(urlEqualTo("/psd2/snsbank/v1/consents"))
                        .willReturn(aResponse().withStatus(200).withBody(expectedBody.toString())));

        TinkHttpClient tinkHttpClient = createWiremockHttpClient();

        VolksbankUrlFactory urlFactory =
                new VolksbankUrlFactory("https://localhost:8888", "snsbank", false);

        VolksbankApiClient apiClient = new VolksbankApiClient(tinkHttpClient, urlFactory);

        URL url =
                new URL(
                        "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback");

        apiClient.consentRequest(url, "l7cafebabecafebabecafebabecafebabe");
    }
}

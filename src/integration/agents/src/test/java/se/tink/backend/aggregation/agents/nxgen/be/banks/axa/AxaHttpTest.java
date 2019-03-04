package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.URL;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class AxaHttpTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort().dynamicHttpsPort());

    private static String REQUEST_JSON =
            "{\"input\":{\"applCd\":\"MOBILEBANK\",\"language\":\"de\",\"UCRid\":\"63474942448027461104511578683880\"}}";
    private static String RESPONSE_JSON =
            "{\"output\": {\"challenge\": \"19800668\", \"errors\": null, \"activationPassword\": \"pYlk51\"}}";

    @Test
    public void testWiremock() throws IOException {
        wireMockRule.stubFor(
                post(urlEqualTo("/AXA_BE_MOBILE_generateUCRChallenge"))
                        .withRequestBody(equalToJson(REQUEST_JSON))
                        .willReturn(aResponse().withStatus(200).withBody(RESPONSE_JSON)));

        CloseableHttpClient httpClient =
                HttpClientBuilder.create()
                        .setSslcontext(buildAllowAnythingSslContext())
                        .setHostnameVerifier(new AllowAllHostnameVerifier())
                        .build();
        HttpPost request =
                new HttpPost(
                        "https://127.0.0.1:"
                                + wireMockRule.httpsPort()
                                + "/AXA_BE_MOBILE_generateUCRChallenge");
        request.setEntity(new StringEntity(REQUEST_JSON));
        HttpResponse httpResponse = httpClient.execute(request);

        System.out.println(toString(httpResponse));

        URL url = new URL("https://esg.services.axabank.be/AXA_BE_MOBILE_generateUCRChallenge");
    }

    private static SSLContext buildAllowAnythingSslContext() {
        try {
            return SSLContexts.custom()
                    .loadTrustMaterial(null, (x509Certificates, s) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IllegalStateException();
        }
    }

    private static String toString(HttpResponse response) throws IOException {
        InputStream stream = response.getEntity().getContent();
        Scanner scanner = new Scanner(stream, "UTF-8");
        String out = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return out;
    }
}

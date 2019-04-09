package se.tink.libraries.jersey.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class InterContainerJerseyClientFactoryTest {

    // can be retrieved using a web browser
    private static final String WIREMOCK_CERTIFICATE_FINGERPRINT =
            "CERTSHA256:" + "660281F6DDF9C1DE6621108B69A985429A5BCE179A4A6CF48959A1BDA6E19C31";

    @Rule
    public WireMockRule server =
            new WireMockRule(WireMockConfiguration.options().dynamicPort().dynamicHttpsPort());

    @Test
    public void testWhenPinningFails() {
        String nonMatchingFingerprint =
                "CERTSHA256:0000000000000000000000000000000000000000000000000000000000000000";
        InterContainerJerseyClientFactory clientFactory =
                new InterContainerJerseyClientFactory(nonMatchingFingerprint);
        Client client = clientFactory.build();
        try {
            client.resource("https://localhost:" + server.httpsPort()).get(String.class);
        } catch (ClientHandlerException e) {
            Assert.assertTrue(e.getCause() instanceof SSLHandshakeException);
            return;
        }
        Assert.fail("Expected exception to have been thrown.");
    }

    @Test
    public void testWhenPinningPasses() {
        server.stubFor(get(urlEqualTo("/")).willReturn(aResponse()));

        InterContainerJerseyClientFactory clientFactory =
                new InterContainerJerseyClientFactory(WIREMOCK_CERTIFICATE_FINGERPRINT);

        Client client = clientFactory.build();
        client.resource("https://localhost:" + server.httpsPort()).get(String.class);
    }

    @Test
    public void testDefaultReadTimeout() {
        InterContainerJerseyClientFactory clientFactory =
                InterContainerJerseyClientFactory.withoutPinning();
        Assert.assertNotNull("Read timeout must be set.", clientFactory.getReadTimeoutMs());
        Assert.assertTrue(
                "Read timeout must be strictly positive.", clientFactory.getReadTimeoutMs() > 0);

        // Double check a constructed client also has the proper properties.
        Client client = clientFactory.build();
        Assert.assertTrue(
                client.getProperties().containsKey("com.sun.jersey.client.property.readTimeout"));
        Assert.assertTrue(
                ((Integer) client.getProperties().get("com.sun.jersey.client.property.readTimeout"))
                        > 0);
    }

    @Test
    public void testDefaultConnectTimeout() {
        InterContainerJerseyClientFactory clientFactory =
                InterContainerJerseyClientFactory.withoutPinning();
        Assert.assertNotNull("Connect timeout must be set.", clientFactory.getReadTimeoutMs());
        Assert.assertTrue(
                "Connect timeout must be strictly positive.", clientFactory.getReadTimeoutMs() > 0);

        // Double check a constructed client also has the proper properties.
        Client client = clientFactory.build();
        Assert.assertTrue(
                client.getProperties()
                        .containsKey("com.sun.jersey.client.property.connectTimeout"));
        Assert.assertTrue(
                ((Integer)
                                client.getProperties()
                                        .get("com.sun.jersey.client.property.connectTimeout"))
                        > 0);
    }

    @Test
    public void testReadTimeout() {
        int responseDelayMs = 1000;
        server.stubFor(
                get(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(responseDelayMs)));

        InterContainerJerseyClientFactory clientFactory =
                InterContainerJerseyClientFactory.withoutPinning();
        clientFactory.setReadTimeoutMs(1);

        Client client = clientFactory.build();
        try {
            client.resource("http://localhost:" + server.port()).get(String.class);
        } catch (ClientHandlerException e) {
            Assert.assertTrue(e.getCause() instanceof SocketTimeoutException);

            // Must validate the message since connect and read timeouts throw the same type of
            // exception.
            Assert.assertEquals("Read timed out", e.getCause().getMessage());

            return;
        }
        Assert.fail("Expected exception to have been thrown.");
    }

    @Test
    @Ignore
    public void testThatNoPinningValidatesTinkCA() {
        InterContainerJerseyClientFactory clientFactory =
                InterContainerJerseyClientFactory.withoutPinning();
        Client client = clientFactory.build();
        client.resource("https://eu-west-1.tink.se/api/v1/user/ping").get(String.class);
    }

    @Test(expected = ClientHandlerException.class)
    public void testThatNoPinningInvalidatesGoogle() {
        InterContainerJerseyClientFactory clientFactory =
                InterContainerJerseyClientFactory.withoutPinning();
        Client client = clientFactory.build();
        client.resource("https://localhost:" + server.httpsPort()).get(String.class);
    }
}

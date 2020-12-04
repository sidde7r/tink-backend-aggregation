package se.tink.libraries.jersey.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import javax.ws.rs.core.MediaType;
import org.junit.Rule;
import org.junit.Test;

public class ClientLoggingFilterTest {

    @Rule
    public WireMockRule server = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Test
    public void shouldLogRequestAndResponse() {
        server.stubFor(post(urlEqualTo("/")).willReturn(ok("{\"a\":\"value\",\"b\":1234}")));

        DefaultClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(JacksonJsonProvider.class);

        Client client = Client.create(cc);
        client.addFilter(new ClientLoggingFilter());
        client.resource("http://localhost:" + server.port())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, new TestEntity());
    }
}

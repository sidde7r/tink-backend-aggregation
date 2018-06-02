package se.tink.backend.client;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;
import se.tink.libraries.http.client.WebResourceFactory;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class ClientAuthorizationConfiguratorTest {

    private final Client client = InterContainerJerseyClientFactory.withoutPinning().build();
    @Rule public WireMockRule server = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Before
    public void setUp() {
        server.stubFor(post(urlEqualTo("/resource"))
                .willReturn(aResponse().withHeader("content-type", "application/json")
                        .withBody("{\"body\": \"content\"}")));
    }

    @Test
    public void clientBearerAuthorization() {
        ClientAuthorizationConfigurator clientAuthenticator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(client);
        clientAuthenticator.setBearerToken("BearerToken");

        Resource resource = WebResourceFactory.newResource(
                Resource.class, client.resource("http://localhost:" + server.port()));
        resource.endpoint();

        server.verify(postRequestedFor(urlEqualTo("/resource"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer BearerToken")));
    }

    @Test
    public void clientBasicAuthorization() {
        ClientAuthorizationConfigurator clientAuthenticator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(client);
        clientAuthenticator.setBasicAuthorization("username", "password");

        Resource resource = WebResourceFactory.newResource(
                Resource.class, client.resource("http://localhost:" + server.port()));
        resource.endpoint();

        server.verify(postRequestedFor(urlEqualTo("/resource"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Basic "
                        + ClientAuthorizationConfigurator.basicCredentials("username", "password"))));
    }

    @Test
    public void resourceBearerAuthorization() {
        WebResource jerseyResource = client.resource("http://localhost:" + server.port());
        ClientAuthorizationConfigurator clientAuthenticator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(jerseyResource);

        clientAuthenticator.setBearerToken("BearerToken");

        Resource resource = WebResourceFactory.newResource(Resource.class, jerseyResource);
        resource.endpoint();

        server.verify(postRequestedFor(urlEqualTo("/resource"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer BearerToken")));
    }

    @Test
    public void resourceBasicAuthorization() {
        WebResource jerseyResource = client.resource("http://localhost:" + server.port());
        ClientAuthorizationConfigurator clientAuthenticator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(jerseyResource);

        clientAuthenticator.setBasicAuthorization("username", "password");

        Resource resource = WebResourceFactory.newResource(Resource.class, jerseyResource);
        resource.endpoint();

        server.verify(postRequestedFor(urlEqualTo("/resource"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Basic "
                    + ClientAuthorizationConfigurator.basicCredentials("username", "password"))));
    }

}

interface Resource {
    @POST
    @Path("/resource")
    void endpoint();
}

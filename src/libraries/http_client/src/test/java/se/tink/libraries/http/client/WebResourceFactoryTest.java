package se.tink.libraries.http.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.libraries.requesttracing.RequestTracer;

public class WebResourceFactoryTest {

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ApiParam {}

    private interface ResourceWithConsumes {
        @POST
        @Path("/resource")
        @Consumes("application/json")
        String endpoint(@ApiParam String body);
    }

    private interface ResourceWithoutConsumes {
        @POST
        @Path("/resource")
        void endpoint(@ApiParam String body);
    }

    @Rule
    public WireMockRule server = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    WebResource resource;

    @Before
    public void setUp() {
        server.stubFor(post(urlEqualTo("/resource")).willReturn(aResponse().withBody("body")));
        resource = new Client().resource("http://localhost:" + server.port());
    }

    @Test
    public void testUnknownAnnotation() {
        ResourceWithConsumes client =
                WebResourceFactory.newResource(ResourceWithConsumes.class, resource);

        assertEquals("body", client.endpoint("body"));

        server.verify(postRequestedFor(urlEqualTo("/resource")).withRequestBody(equalTo("body")));
    }

    @Test
    public void setsJsonWhenContentTypeNotSpecified() {
        ResourceWithoutConsumes client =
                WebResourceFactory.newResource(ResourceWithoutConsumes.class, resource);

        client.endpoint("body");

        server.verify(
                postRequestedFor(urlEqualTo("/resource"))
                        .withHeader("content-type", equalTo("application/json")));
    }

    @Test
    public void setsConsumesDeclarationWhenContentTypeSpecified() {
        ResourceWithConsumes client =
                WebResourceFactory.newResource(ResourceWithConsumes.class, resource);

        client.endpoint("body");

        server.verify(
                postRequestedFor(urlEqualTo("/resource"))
                        .withHeader("content-type", equalTo("application/json")));
    }

    @Test
    public void addRequestIdHeader() {
        RequestTracer.startTracing(Optional.of("requestId"));
        WebResourceFactory.newResource(ResourceWithoutConsumes.class, resource)
                .endpoint("requestBody");

        server.verify(
                postRequestedFor(urlEqualTo("/resource"))
                        .withHeader(RequestTracingFilter.REQUEST_ID_HEADER, equalTo("requestId")));
    }

    @Test
    public void noHeaderWhenRequestIdNotPresent() {
        assertNull(RequestTracer.getRequestId());
        WebResourceFactory.newResource(ResourceWithoutConsumes.class, resource)
                .endpoint("requestBody");

        server.verify(
                postRequestedFor(urlEqualTo("/resource"))
                        .withoutHeader(RequestTracingFilter.REQUEST_ID_HEADER));
    }

    @Test
    public void cleanHeadersAfterEachRequest() {
        RequestTracer.startTracing(Optional.of("requestId"));
        WebResourceFactory.newResource(ResourceWithoutConsumes.class, resource)
                .endpoint("requestBody");
        RequestTracer.stopTracing();

        WebResourceFactory.newResource(ResourceWithoutConsumes.class, resource)
                .endpoint("requestBody");
        server.verify(
                postRequestedFor(urlEqualTo("/resource"))
                        .withoutHeader(RequestTracingFilter.REQUEST_ID_HEADER));
    }
}

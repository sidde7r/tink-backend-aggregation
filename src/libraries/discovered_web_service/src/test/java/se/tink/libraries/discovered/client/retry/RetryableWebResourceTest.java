package se.tink.libraries.discovered.client.retry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.api.client.util.Sleeper;
import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import org.junit.Rule;
import org.junit.Test;
import se.tink.libraries.discovered.RetryableWebResource;
import se.tink.libraries.http.client.WebResourceFactory;

public class RetryableWebResourceTest {

    @Rule
    public final WireMockRule server =
            new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Rule
    public final WireMockRule unavailableServer =
            new WireMockRule(WireMockConfiguration.options().dynamicPort());

    private interface Resource {
        @GET
        @Path("/resource")
        String endpoint();
    }

    @Test(expected = ClientHandlerException.class)
    public void testFailing() throws URISyntaxException {
        String failingUri = "https://nonexistentdomain";
        String fallbackUri = "https://nonexistentdomain2";

        ArrayList<RetryableWebResource.Candidate> fallbacks = Lists.newArrayList();
        fallbacks.add(RetryableWebResource.Candidate.fromURI(new URI(fallbackUri)));

        FakeTime fakeTime = new FakeTime();
        WebResource resource = new Client().resource(failingUri);
        RetryableWebResource.decorate("testClient", fallbacks, resource, fakeTime, fakeTime);
        Resource client = WebResourceFactory.newResource(Resource.class, resource);

        client.endpoint();
    }

    @Test
    public void testUnconnectableHost() throws URISyntaxException {
        String failingUri = "http://localhost:" + unavailableServer.port();
        // shut down the server to get connection refused error for failingUri
        unavailableServer.stop();

        String fallbackUri = "http://localhost:" + server.port();
        server.stubFor(get(urlEqualTo("/resource")).willReturn(aResponse().withBody("body")));

        ArrayList<RetryableWebResource.Candidate> fallbacks = Lists.newArrayList();
        fallbacks.add(RetryableWebResource.Candidate.fromURI(new URI(fallbackUri)));

        FakeTime fakeTime = new FakeTime();
        WebResource resource = new Client().resource(failingUri);
        RetryableWebResource.decorate("testClient", fallbacks, resource, fakeTime, fakeTime);
        Resource client = WebResourceFactory.newResource(Resource.class, resource);

        assertEquals("body", client.endpoint());
    }

    @Test
    public void testUnresolvableHostname() throws URISyntaxException {
        String failingURI = "https://nonexistentdomain";
        String fallbackUri = "http://localhost:" + server.port();
        server.stubFor(get(urlEqualTo("/resource")).willReturn(aResponse().withBody("body")));

        ArrayList<RetryableWebResource.Candidate> fallbacks = Lists.newArrayList();
        fallbacks.add(RetryableWebResource.Candidate.fromURI(new URI(fallbackUri)));

        FakeTime fakeTime = new FakeTime();
        WebResource resource = new Client().resource(failingURI);
        RetryableWebResource.decorate("testClient", fallbacks, resource, fakeTime, fakeTime);
        Resource client = WebResourceFactory.newResource(Resource.class, resource);

        assertEquals("body", client.endpoint());
    }

    @Test
    public void testURIModificationBenchmark() throws URISyntaxException {
        Stopwatch timer = Stopwatch.createStarted();
        URI uri = new URI("http://10.11.1.51:9091/user/ping");
        for (int i = 0; i < 40000; i++) {
            UriBuilder.fromUri(uri).host("10.11.1.52").port(9091).scheme("http").build();
        }
        System.out.println(timer.stop()); // 667.9 ms
    }

    @Test
    public void test503ServiceUnavailableRetry() throws URISyntaxException {
        String unavailableUri = "http://localhost:" + unavailableServer.port();
        unavailableServer.stubFor(
                get(urlEqualTo("/resource"))
                        .willReturn(aResponse().withStatus(SC_SERVICE_UNAVAILABLE)));

        // no mapping present
        String notFoundUri = "http://localhost:" + server.port();

        ImmutableList<RetryableWebResource.Candidate> fallbacks =
                ImmutableList.of(
                        RetryableWebResource.Candidate.fromURI(new URI(unavailableUri)),
                        RetryableWebResource.Candidate.fromURI(new URI(notFoundUri)));

        FakeTime fakeTime = new FakeTime();
        WebResource resource = new Client().resource(unavailableUri);
        RetryableWebResource.decorate("testClient", fallbacks, resource, fakeTime, fakeTime);
        Resource client = WebResourceFactory.newResource(Resource.class, resource);

        try {
            client.endpoint();
        } catch (UniformInterfaceException e) {
            assertEquals(SC_NOT_FOUND, e.getResponse().getStatus());
        }
    }

    private static class FakeTime extends Ticker implements Sleeper {

        // This should be, for the sake of realistic testing, something close to the HTTP call
        // latencies we have between
        // containers.
        private static final long READ_DURATION_DELTA_NANOS = TimeUnit.MILLISECONDS.toNanos(50);

        private long now;

        public FakeTime() {
            this.now = System.nanoTime();
        }

        @Override
        public void sleep(long millis) throws InterruptedException {
            this.now += TimeUnit.MILLISECONDS.toNanos(millis);
        }

        @Override
        public long read() {
            // Faking some time duration between read() calls.
            now += READ_DURATION_DELTA_NANOS;

            return now;
        }
    }
}

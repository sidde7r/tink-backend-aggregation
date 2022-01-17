package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.OutBoundHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration.SwedbankPsd2Configuration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils.SignatureProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SwedbankFallbackHttpFilterTest {

    SwedbankFallbackHttpFilter swedbankFallbackHttpFilter;
    Filter nextFilter;
    private static final String ORG_ID = "DUMMY_ORG_ID";
    private static final String QSEALC_EXAMPLE =
            "MIIExDCCA6ygAwIBAgIJAK0JmDc/YXWsMA0GCSqGSIb3DQEBBQUAMIGcMQswCQYD"
                    + "VQQGEwJJTjELMAkGA1UECBMCQVAxDDAKBgNVBAcTA0hZRDEZMBcGA1UEChMQUm9j"
                    + "a3dlbGwgY29sbGluczEcMBoGA1UECxMTSW5kaWEgRGVzaWduIENlbnRlcjEOMAwG"
                    + "A1UEAxMFSU1BQ1MxKTAnBgkqhkiG9w0BCQEWGmJyYWphbkBSb2Nrd2VsbGNvbGxp"
                    + "bnMuY29tMB4XDTExMDYxNjE0MTQyM1oXDTEyMDYxNTE0MTQyM1owgZwxCzAJBgNV"
                    + "BAYTAklOMQswCQYDVQQIEwJBUDEMMAoGA1UEBxMDSFlEMRkwFwYDVQQKExBSb2Nr"
                    + "d2VsbCBjb2xsaW5zMRwwGgYDVQQLExNJbmRpYSBEZXNpZ24gQ2VudGVyMQ4wDAYD"
                    + "VQQDEwVJTUFDUzEpMCcGCSqGSIb3DQEJARYaYnJhamFuQFJvY2t3ZWxsY29sbGlu"
                    + "cy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDfjHgUAsbXQFkF"
                    + "hqv8OTHSzuj+8SKGh49wth3UcH9Nk/YOug7ZvI+tnOcrCZdeG2Ot8Y19Wusf59Y7"
                    + "q61jSbDWt+7u7P0ylWWcQfCE9IHSiJIaKAklMu2qGB8bFSPqDyVJuWSwcSXEb9C2"
                    + "xJsabfgJr6mpfWjCOKd58wFprf0RF58pWHyBqBOiZ2U20PKhq8gPJo/pEpcnXTY0"
                    + "x8bw8LZ3SrrIQZ5WntFKdB7McFKG9yFfEhUamTKOffQ2Y+SDEGVDj3eshF6+Fxgj"
                    + "8plyg3tZPRLSHh5DR42HTc/35LA52BvjRMWYzrs4nf67gf652pgHh0tFMNMTMgZD"
                    + "rpTkyts9AgMBAAGjggEFMIIBATAdBgNVHQ4EFgQUG0cLBjouoJPM8dQzKUQCZYNY"
                    + "y8AwgdEGA1UdIwSByTCBxoAUG0cLBjouoJPM8dQzKUQCZYNYy8ChgaKkgZ8wgZwx"
                    + "CzAJBgNVBAYTAklOMQswCQYDVQQIEwJBUDEMMAoGA1UEBxMDSFlEMRkwFwYDVQQK"
                    + "ExBSb2Nrd2VsbCBjb2xsaW5zMRwwGgYDVQQLExNJbmRpYSBEZXNpZ24gQ2VudGVy"
                    + "MQ4wDAYDVQQDEwVJTUFDUzEpMCcGCSqGSIb3DQEJARYaYnJhamFuQFJvY2t3ZWxs"
                    + "Y29sbGlucy5jb22CCQCtCZg3P2F1rDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEB"
                    + "BQUAA4IBAQCyYZxEzn7203no9TdhtKDWOFRwzYvY2kZppQ/EpzF+pzh8LdBOebr+"
                    + "DLRXNh2NIFaEVV0brpQTI4eh6b5j7QyF2UmA6+44zmku9LzS9DQVKGLhIleB436K"
                    + "ARoWRqxlEK7TF3TauQfaalGH88ZWoDjqqEP/5oWeQ6pr/RChkCHkBSgq6FfGGSLd"
                    + "ktgFcF0S9U7Ybii/MD+tWMImK8EE3GGgs876yqX/DDhyfW8DfnNZyl35VF/80j/s"
                    + "0Lj3F7Po1zsaRbQlhOK5rzRVQA2qnsa4IcQBuYqBWiB6XojPgu9PpRSL7ure7sj6"
                    + "gRQT0OIU5vXzsmhjqKoZ+dBlh1FpSOX2";

    @Before
    public void setUp() {
        RandomValueGenerator randomValueGenerator = mock(RandomValueGenerator.class);
        SwedbankPsd2Configuration configuration = mock(SwedbankPsd2Configuration.class);
        AgentsServiceConfiguration agentsServiceConfiguration =
                mock(AgentsServiceConfiguration.class);
        EidasIdentity eidasIdentity = mock(EidasIdentity.class);
        SignatureProvider signatureProvider = mock(SignatureProvider.class);
        nextFilter = mock(Filter.class);
        swedbankFallbackHttpFilter =
                new SwedbankFallbackHttpFilter(
                        randomValueGenerator,
                        configuration,
                        agentsServiceConfiguration,
                        eidasIdentity,
                        QSEALC_EXAMPLE,
                        signatureProvider);
        swedbankFallbackHttpFilter.setNext(nextFilter);
    }

    @Test
    public void shouldAddHeadersAndReturnResponseIfTransactionEndpoint() {
        HttpRequest request = setupHttpRequest("/v5/engagement/transactions");
        Assert.assertEquals(1, request.getHeaders().size());

        swedbankFallbackHttpFilter.handle(request);

        Assert.assertEquals(5, request.getHeaders().size());
    }

    @Test
    public void shouldNotAddDuplicatesHeadersIfRetryRequest() {
        HttpRequest request = setupHttpRequest("/v5/engagement/transactions");
        Assert.assertEquals(1, request.getHeaders().size());

        swedbankFallbackHttpFilter.handle(request);

        Assert.assertEquals(5, request.getHeaders().size());
        Assert.assertEquals(1, request.getHeaders().get("tpp-x-request-id").size());

        swedbankFallbackHttpFilter.handle(request);

        Assert.assertEquals(1, request.getHeaders().get("tpp-x-request-id").size());
    }

    @Test
    public void shouldAddHeadersAndReturnResponseIfNotTransactionEndpoint() {
        HttpResponse httpResponse = setupOKHttpResponse();
        HttpRequest request = setupHttpRequest("random url");
        when(nextFilter.handle(request)).thenReturn(httpResponse);

        Assert.assertEquals(1, request.getHeaders().size());

        swedbankFallbackHttpFilter.handle(request);

        Assert.assertEquals(5, request.getHeaders().size());
    }

    @Test
    public void shouldThrowIfServerErrorAndNotTransactionEndpoint() {
        HttpResponse httpResponse = setupInternalServerErrorResponse();
        HttpRequest request = setupHttpRequest("random url");
        when(nextFilter.handle(request)).thenReturn(httpResponse);

        Throwable throwable = catchThrowable(() -> swedbankFallbackHttpFilter.handle(request));

        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Http status: 500, body: this is an error message");
    }

    @Test
    public void shouldNotThrowIfServerErrorAndTransactionEndpoint() {
        HttpResponse httpResponse = setupInternalServerErrorResponse();
        HttpRequest request = setupHttpRequest("/v5/engagement/transactions");
        when(nextFilter.handle(request)).thenReturn(httpResponse);
        Assert.assertEquals(1, request.getHeaders().size());

        swedbankFallbackHttpFilter.handle(request);

        Assert.assertEquals(5, request.getHeaders().size());
    }

    private HttpResponse setupInternalServerErrorResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(httpResponse.getBody(String.class)).thenReturn("this is an error message");
        return httpResponse;
    }

    private HttpResponse setupOKHttpResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_OK);
        return httpResponse;
    }

    private HttpRequest setupHttpRequest(String url) {
        return new HttpRequestImpl(HttpMethod.GET, new URL(url), getHeaders(), null);
    }

    private MultivaluedMap<String, Object> getHeaders() {
        MultivaluedMap<String, Object> headers = new OutBoundHeaders();
        headers.putSingle("key", ORG_ID);
        return headers;
    }
}

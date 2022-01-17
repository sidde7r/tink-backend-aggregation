package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.nxgen.http.NextGenRequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class HandelsbankenApiClientTest {

    private HandelsbankenApiClient apiClient;
    private TinkHttpClient client;
    private HandelsbankenSEConfiguration handelsbankenConfiguration;
    private RequestBuilder requestBuilder;
    private URL url;

    @Before
    public void setUp() {
        client = mock(TinkHttpClient.class);
        requestBuilder = getRequestBuilder();
        url = new URL("thisIsAnUrl");
        handelsbankenConfiguration = mock(HandelsbankenSEConfiguration.class);
        when(handelsbankenConfiguration.getAppVersion()).thenReturn("APP_VERSION");
        when(handelsbankenConfiguration.getDeviceModel()).thenReturn("DEVICE_MODEL");
        when(client.request(url)).thenReturn(requestBuilder);
    }

    @Test
    public void shouldNotAddXmlHeaderIfProviderIsCardReader() {
        apiClient =
                new HandelsbankenSEApiClient(client, handelsbankenConfiguration, "handelsbanken");
        RequestBuilder result = apiClient.createRequestWithOrWithoutXmlHeader(url);

        HttpRequest httpRequest = result.build(HttpMethod.GET);

        Assert.assertEquals(1, httpRequest.getHeaders().get("Accept").size());
        Assert.assertTrue(
                httpRequest.getHeaders().get("Accept").contains(MediaType.APPLICATION_JSON));
        Assert.assertFalse(
                httpRequest.getHeaders().get("Accept").contains(MediaType.APPLICATION_XML));
    }

    @Test
    public void shouldAddXmlHeaderIfProviderIsNotCardReader() {
        apiClient =
                new HandelsbankenSEApiClient(
                        client, handelsbankenConfiguration, "handelsbanken-bankid");

        RequestBuilder result = apiClient.createRequestWithOrWithoutXmlHeader(url);

        HttpRequest httpRequest = result.build(HttpMethod.GET);

        Assert.assertEquals(2, httpRequest.getHeaders().get("Accept").size());
        Assert.assertTrue(
                httpRequest.getHeaders().get("Accept").contains(MediaType.APPLICATION_JSON));
        Assert.assertTrue(
                httpRequest.getHeaders().get("Accept").contains(MediaType.APPLICATION_XML));
    }

    private RequestBuilder getRequestBuilder() {
        return requestBuilder = new NextGenRequestBuilder(null, "test", null);
    }
}

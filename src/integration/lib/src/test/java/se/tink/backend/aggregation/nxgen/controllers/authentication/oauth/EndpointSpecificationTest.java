package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class EndpointSpecificationTest {

    @Test
    public void shouldReturnUrl() throws MalformedURLException {
        // given
        final URL url = new URL("http:://127.0.0.1");
        EndpointSpecification objectUnderTest = new EndpointSpecification(url);
        // when
        URL result = objectUnderTest.getUrl();
        // then
        Assert.assertEquals(url, result);
    }

    @Test
    public void shouldReturnHeaders() throws MalformedURLException {
        // given
        EndpointSpecification objectUnderTest =
                new EndpointSpecification(new URL("http://127.0.0.1"))
                        .withHeader("key1", "value1")
                        .withHeader("key2", "value2");
        // when
        Map<String, Object> result = objectUnderTest.getHeaders();
        // then
        Assert.assertEquals("value1", result.get("key1"));
        Assert.assertEquals("value2", result.get("key2"));
    }

    @Test
    public void shouldReturnClientSpecificParams() throws MalformedURLException {
        // given
        EndpointSpecification objectUnderTest =
                new EndpointSpecification(new URL("http://127.0.0.1"))
                        .withClientSpecificParameter("key1", "value1")
                        .withClientSpecificParameter("key2", "value2");
        // when
        Map<String, String> result = objectUnderTest.getClientSpecificParameters();
        // then
        Assert.assertEquals("value1", result.get("key1"));
        Assert.assertEquals("value2", result.get("key2"));
    }
}

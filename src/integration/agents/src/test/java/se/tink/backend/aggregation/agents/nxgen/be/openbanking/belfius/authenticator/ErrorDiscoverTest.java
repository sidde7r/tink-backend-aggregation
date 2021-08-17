package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class ErrorDiscoverTest {

    private HttpResponseException exception;
    private HttpResponse response;

    @Before
    public void init() {
        exception = Mockito.mock(HttpResponseException.class);
        response = Mockito.mock(HttpResponse.class);
        Mockito.when(exception.getResponse()).thenReturn(response);
    }

    @Test
    public void channelNotPermittedErrorDiscoveryTest() {
        // given
        Mockito.when(response.getStatus()).thenReturn(403);
        Mockito.when(response.getBody(String.class))
                .thenReturn(
                        "{\"error_description\":\"This account can't be consulted via electronic channel\",\"error_code\":\"20003\",\"error\":\"channel_not_permitted\"}");

        // when
        boolean result = ErrorDiscover.isChannelNotPermitted(exception);

        // then
        Assert.assertTrue(result);
    }
}

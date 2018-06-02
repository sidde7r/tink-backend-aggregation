package se.tink.backend.webhook;

import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.TestUtils;
import se.tink.backend.core.oauth2.OAuth2WebHook;
import se.tink.backend.webhook.rpc.WebHookRequest;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricRegistry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebHookExecutorTest {

    private Client client;
    private WebResource.Builder builder;
    private ClientResponse okResponse;
    private ClientResponse noContentResponse;
    private ClientResponse badGatewayResponse;

    private StopStrategy stopStrategy;
    private WaitStrategy waitStrategy;
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        client = mock(Client.class);

        metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.meter(any())).thenReturn(mock(Counter.class));

        builder = TestUtils.setUpJerseyClient(client);

        stopStrategy = StopStrategies.stopAfterAttempt(3);
        waitStrategy = WaitStrategies.fixedWait(5, TimeUnit.MILLISECONDS);

        okResponse = mock(ClientResponse.class);
        noContentResponse = mock(ClientResponse.class);
        badGatewayResponse = mock(ClientResponse.class);

        when(okResponse.getStatus()).thenReturn(200);
        when(noContentResponse.getStatus()).thenReturn(204);
        when(badGatewayResponse.getStatus()).thenReturn(502);
    }

    @Test
    public void verifyPostWebHookIsOnlyCalledOnceOnOKResponse() {

        when(builder.post(eq(ClientResponse.class), any(WebHookRequest.class))).thenReturn(okResponse);

        new WebHookExecutor(client, stopStrategy, waitStrategy, metricRegistry)
                .execute(request("https://www.some-url.com"));

        verify(builder, times(1)).post(eq(ClientResponse.class), any(WebHookRequest.class));
    }

    @Test
    public void verifyPostWebHookIsOnlyCalledOnceOnNoContentResponse() {
        when(builder.post(eq(ClientResponse.class), any(WebHookRequest.class))).thenReturn(noContentResponse);

        new WebHookExecutor(client, stopStrategy, waitStrategy, metricRegistry)
                .execute(request("https://www.some-url.com"));

        verify(builder, times(1)).post(eq(ClientResponse.class), any(WebHookRequest.class));
    }

    @Test
    public void verifyPostWebHookIsOnlyCalledTwiceIfFirstResponseIsFailed() {
        when(builder.post(eq(ClientResponse.class), any(WebHookRequest.class)))
                .thenReturn(badGatewayResponse, noContentResponse);

        new WebHookExecutor(client, stopStrategy, waitStrategy, metricRegistry)
                .execute(request("https://www.some-url.com"));

        verify(builder, times(2)).post(eq(ClientResponse.class), any(WebHookRequest.class));
    }

    @Test
    public void verifyPostStopAsStopStrategySuggestsIfResponseAlwaysIsFailed() {
        when(builder.post(eq(ClientResponse.class), any(WebHookRequest.class))).thenReturn(badGatewayResponse);

        new WebHookExecutor(client, stopStrategy, waitStrategy, metricRegistry)
                .execute(request("https://www.some-url.com"));

        verify(builder, times(3)).post(eq(ClientResponse.class), any(WebHookRequest.class));
    }

    private static WebHookRequest request(String url) {
        OAuth2WebHook webHook = new OAuth2WebHook();
        webHook.setUrl(url);

        WebHookRequest request = new WebHookRequest();
        request.setWebhook(webHook);

        return request;
    }
}

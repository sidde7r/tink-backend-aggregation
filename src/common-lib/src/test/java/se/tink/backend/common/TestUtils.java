package se.tink.backend.common;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.util.Locale;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.mockito.Mockito;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static WebResource.Builder setUpJerseyClient(Client client) {

        WebResource webResource = mock(WebResource.class);
        WebResource.Builder builder = mock(WebResource.Builder.class);

        when(webResource.accept(any(MediaType[].class))).thenReturn(builder);
        when(webResource.accept(any(String[].class))).thenReturn(builder);
        when(webResource.acceptLanguage(any(Locale[].class))).thenReturn(builder);
        when(webResource.acceptLanguage(any(String[].class))).thenReturn(builder);
        when(webResource.cookie(any(Cookie.class))).thenReturn(builder);
        when(webResource.entity(any())).thenReturn(builder);
        when(webResource.entity(any(), any(MediaType.class))).thenReturn(builder);
        when(webResource.entity(any(), any(String.class))).thenReturn(builder);
        when(webResource.getRequestBuilder()).thenReturn(builder);
        when(webResource.type(any(MediaType.class))).thenReturn(builder);
        when(webResource.type(any(String.class))).thenReturn(builder);
        when(webResource.type(any(String.class))).thenReturn(builder);

        when(webResource.path(anyString())).thenReturn(webResource);
        when(webResource.queryParam(anyString(), anyString())).thenReturn(webResource);
        when(webResource.queryParams(any(MultivaluedMap.class))).thenReturn(webResource);
        when(webResource.uri(any(URI.class))).thenReturn(webResource);

        when(client.resource(anyString())).thenReturn(webResource);

        return builder;
    }

    public static ServiceContext mockSystemContext(ServiceConfiguration conf) {

        ServiceContext context = mock(ServiceContext.class);
        UserRepository user = mock(UserRepository.class);
        FraudDetailsRepository fraudDetails = mock(FraudDetailsRepository.class);

        when(context.getRepository(FraudDetailsRepository.class)).thenReturn(fraudDetails);
        when(context.getRepository(UserRepository.class)).thenReturn(user);

        when(context.getConfiguration()).thenReturn(conf);

        return context;
    }

    public static MetricRegistry mockMetricRegistry() {
        MetricRegistry registry = Mockito.mock(MetricRegistry.class);
        Timer timer = Mockito.mock(Timer.class);
        Counter meter = Mockito.mock(Counter.class);
        Histogram histogram = Mockito.mock(Histogram.class);
        when(registry.timer(any(MetricId.class))).thenReturn(timer);
        when(registry.meter(any(MetricId.class))).thenReturn(meter);
        when(registry.histogram(any(MetricId.class))).thenReturn(histogram);

        return registry;
    }
}

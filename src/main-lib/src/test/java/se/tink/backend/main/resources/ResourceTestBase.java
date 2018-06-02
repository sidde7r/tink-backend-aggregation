package se.tink.backend.main.resources;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.AnalyticsConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;
import se.tink.backend.common.config.IntercomConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.ThreadPoolsConfiguration;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.Market;
import se.tink.libraries.metrics.MetricRegistry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceTestBase {

    protected ServiceContext serviceContext;
    protected EventRepository eventRepository;
    protected UserOriginRepository userOriginRepository;
    protected UserStateRepository userStateRepository;
    protected CurrencyRepository currencyRepository;
    protected MarketRepository marketRepository;
    private ThreadPoolsConfiguration threadPoolsConfiguration;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        ServiceConfiguration serviceConfiguration = mock(ServiceConfiguration.class);

        currencyRepository = mock(CurrencyRepository.class);
        eventRepository = mock(EventRepository.class);
        marketRepository = mock(MarketRepository.class);
        userOriginRepository = mock(UserOriginRepository.class);
        userStateRepository = mock(UserStateRepository.class);
        ListenableThreadPoolExecutor<Runnable> executor = (ListenableThreadPoolExecutor<Runnable>) mock(
                ListenableThreadPoolExecutor.class);
        serviceContext = mock(ServiceContext.class);
        threadPoolsConfiguration = mock(ThreadPoolsConfiguration.class);
        AnalyticsConfiguration analyticsConfig = mock(AnalyticsConfiguration.class);
        EndpointsConfiguration endpointsConfiguration = mock(EndpointsConfiguration.class);
        EndpointConfiguration endpointConfiguration = mock(EndpointConfiguration.class);
        MetricRegistry metricRegistry = mock(MetricRegistry.class);

        when(analyticsConfig.getIntercom()).thenReturn(new IntercomConfiguration());
        when(endpointConfiguration.getUrl()).thenReturn("https://www.tink.se/api/v1");
        when(endpointsConfiguration.getAPI()).thenReturn(endpointConfiguration);
        when(marketRepository.findAll()).thenReturn(mockMarkets());
        when(serviceConfiguration.getAnalytics()).thenReturn(analyticsConfig);
        when(serviceConfiguration.getThreadPools()).thenReturn(threadPoolsConfiguration);
        when(serviceConfiguration.getEndpoints()).thenReturn(endpointsConfiguration);
        when(threadPoolsConfiguration.getMaxThreadsContextGeneration()).thenReturn(1);

        when(serviceContext.getConfiguration()).thenReturn(serviceConfiguration);
        when(serviceContext.getTrackingExecutorService()).thenReturn(executor);

        when(serviceContext.getRepository(EventRepository.class)).thenReturn(eventRepository);
        when(serviceContext.getRepository(UserOriginRepository.class)).thenReturn(userOriginRepository);
        when(serviceContext.getRepository(UserStateRepository.class)).thenReturn(userStateRepository);
        when(serviceContext.getRepository(CurrencyRepository.class)).thenReturn(currencyRepository);
        when(serviceContext.getRepository(MarketRepository.class)).thenReturn(marketRepository);
    }

    private static List<Market> mockMarkets() {
        Market market = new Market("SE", null, null, null, null, null, null, null, null, null, 0, true, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
        List<Market> markets = new ArrayList<Market>();
        markets.add(market);
        return markets;
    }
}

package se.tink.backend.aggregation.provider.configuration.client;

import com.sun.jersey.api.client.ClientHandlerException;
import org.junit.Before;
import org.junit.Test;

public class ClientProviderServiceFactoryTest {
    private ClientProviderServiceFactory factory;

    @Before
    public void setUp(){
        factory = ClientProviderServiceFactory.buildWithoutPinning("http://127.0.0.1:31415");
    }

    @Test(expected = ClientHandlerException.class)
    public void testMonitoringService(){
        factory.getMonitoringService().ping();
    }
}

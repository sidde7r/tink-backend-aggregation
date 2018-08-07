package se.tink.backend.aggregation.provider.configuration.client;

import com.sun.jersey.api.client.ClientHandlerException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ClientProviderServiceFactoryTest {
    private ClientProviderServiceFactory factory;

    @Before
    public void setUp(){
        ArrayList<String> certificateList = new ArrayList<>();
        certificateList.add("CERTSHA256:A0A8FBAC442F4A58AC56CB1D0C185F93EE119A1B026BA3F046B0CA99EC165390");
        factory = new ClientProviderServiceFactory(certificateList,"http://127.0.0.1:31415");
    }

    @Test(expected = ClientHandlerException.class)
    public void testMonitoringService(){
        factory.getMonitoringService().ping();
    }
}

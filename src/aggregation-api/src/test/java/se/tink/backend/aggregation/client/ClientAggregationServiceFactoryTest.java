package se.tink.backend.aggregation.client;

import com.sun.jersey.api.client.ClientHandlerException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.utils.StringUtils;

public class ClientAggregationServiceFactoryTest {

    private ClientAggregationServiceFactory factory;

    @Before
    public void setUp() {
        // Expected: no service is running on port 31415.
        factory = ClientAggregationServiceFactory
                .buildWithoutPinning("http://127.0.0.1:31415/");
    }

    @Test(expected = ClientHandlerException.class)
    public void testBasicPing() {
        factory.getAggregationService().ping();
    }

    @Test(expected = ClientHandlerException.class)
    public void testBasicPingOfRouted() {
        User user = new User();
        user.setId(StringUtils.generateUUID());
        factory.getAggregationService(user).ping();
    }

    @Test(expected = ClientHandlerException.class)
    public void testClusterIdAsNull() {
        User user = new User();
        user.setId(StringUtils.generateUUID());
        factory.getAggregationService(user).ping();
    }

}

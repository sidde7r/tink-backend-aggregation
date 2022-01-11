package se.tink.backend.aggregation.aggregationcontroller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.jersey.api.client.Client;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InterClusterClientProviderTest {

    @Mock private InterClusterClientFactory factory;
    @InjectMocks private InterClusterClientProvider provider;

    @Test
    public void createShouldInvokeFactoryOnceOnFirstCall() {
        // given
        String clusterId = "clusterId";
        Client expectedClient = mock(Client.class);
        when(factory.create(clusterId)).thenReturn(expectedClient);

        // when
        Client client = provider.getByClusterId(clusterId);

        // then
        assertEquals(expectedClient, client);
        verify(factory).create(eq(clusterId));
    }

    @Test
    public void createShouldNotInvokeFactoryOnNextCall() {
        // given
        String clusterId = "clusterId";
        Client expectedClient = mock(Client.class);
        when(factory.create(clusterId)).thenReturn(expectedClient);
        provider.getByClusterId(clusterId);

        // when
        Client client = provider.getByClusterId(clusterId);

        // then
        assertEquals(expectedClient, client);
        verify(factory).create(eq(clusterId));
    }
}

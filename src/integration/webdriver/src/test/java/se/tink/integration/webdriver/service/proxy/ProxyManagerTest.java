package se.tink.integration.webdriver.service.proxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.browserup.bup.BrowserUpProxy;
import org.junit.Before;
import org.junit.Test;

public class ProxyManagerTest {

    /*
    Mocks
     */
    private BrowserUpProxy proxy;
    private ProxyFilterRegistry proxyFilterRegistry;

    @Before
    public void setup() {
        proxy = mock(BrowserUpProxy.class);
        proxyFilterRegistry = mock(ProxyFilterRegistry.class);
    }

    @Test
    public void should_add_proxy_filter_registry_for_requests() {
        // given
        ProxyManager proxyManager = new ProxyManagerImpl(proxy, proxyFilterRegistry);

        // when
        proxyManager.enableRequestsFiltering();

        // then
        verify(proxy).addRequestFilter(proxyFilterRegistry);
        verifyNoMoreInteractions(proxy);
    }

    @Test
    public void should_add_proxy_filter_registry_for_responses() {
        // given
        ProxyManager proxyManager = new ProxyManagerImpl(proxy, proxyFilterRegistry);

        // when
        proxyManager.enableResponseFiltering();

        // then
        verify(proxy).addResponseFilter(proxyFilterRegistry);
        verifyNoMoreInteractions(proxy);
    }
}

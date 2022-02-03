package se.tink.integration.webdriver.service.proxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    public void should_add_proxy_filter_as_request_and_response_filter() {
        // when
        new ProxyManagerImpl(proxy, proxyFilterRegistry);

        // then
        verify(proxy).addRequestFilter(proxyFilterRegistry);
        verify(proxy).addResponseFilter(proxyFilterRegistry);
    }
}

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
    private ProxyFilter proxyFilter;

    @Before
    public void setup() {
        proxy = mock(BrowserUpProxy.class);
        proxyFilter = mock(ProxyFilter.class);
    }

    @Test
    public void should_add_proxy_filter_as_request_and_response_filter() {
        // when
        new ProxyManagerImpl(proxy, proxyFilter);

        // then
        verify(proxy).addRequestFilter(proxyFilter);
        verify(proxy).addResponseFilter(proxyFilter);
    }
}

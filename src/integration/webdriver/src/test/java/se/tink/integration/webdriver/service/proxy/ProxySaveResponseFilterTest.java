package se.tink.integration.webdriver.service.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class ProxySaveResponseFilterTest {

    private ProxySaveResponseMatcher saveResponseMatcher;
    private ProxySaveResponseFilter saveResponseProxyFilter;

    @Before
    public void setup() {
        saveResponseMatcher = mock(ProxySaveResponseMatcher.class);
        saveResponseProxyFilter = new ProxySaveResponseFilter(saveResponseMatcher);
    }

    @Test
    public void should_save_matching_response() {
        // given
        ProxyResponse proxyResponse = mock(ProxyResponse.class);
        when(saveResponseMatcher.matchesResponse(proxyResponse)).thenReturn(true);

        // when
        saveResponseProxyFilter.handleResponse(proxyResponse);

        boolean hasResponse = saveResponseProxyFilter.hasResponse();
        Optional<ProxyResponse> response =
                saveResponseProxyFilter.waitForResponse(0, TimeUnit.MILLISECONDS);

        // then
        assertThat(hasResponse).isTrue();
        assertThat(response).isPresent();
    }

    @Test
    public void should_not_save_not_matching_response() {
        // given
        ProxyResponse proxyResponse = mock(ProxyResponse.class);
        when(saveResponseMatcher.matchesResponse(proxyResponse)).thenReturn(false);

        // when
        saveResponseProxyFilter.handleResponse(proxyResponse);

        boolean hasResponse = saveResponseProxyFilter.hasResponse();
        Optional<ProxyResponse> response =
                saveResponseProxyFilter.waitForResponse(0, TimeUnit.MILLISECONDS);

        // then
        assertThat(hasResponse).isFalse();
        assertThat(response).isEmpty();
    }

    @Test
    public void should_find_response_when_we_wait_for_it_first_and_it_is_saved_later() {
        // given
        ProxyResponse proxyResponse = mock(ProxyResponse.class);
        when(saveResponseMatcher.matchesResponse(proxyResponse)).thenReturn(true);

        // when
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(
                () -> saveResponseProxyFilter.handleResponse(proxyResponse),
                100,
                TimeUnit.MILLISECONDS);

        boolean hasResponse1 = saveResponseProxyFilter.hasResponse();
        Optional<ProxyResponse> response =
                saveResponseProxyFilter.waitForResponse(200, TimeUnit.MILLISECONDS);
        boolean hasResponse2 = saveResponseProxyFilter.hasResponse();

        // then
        assertThat(hasResponse1).isFalse();
        assertThat(response).isPresent();
        assertThat(hasResponse2).isTrue();
    }
}

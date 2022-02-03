package se.tink.integration.webdriver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.service.proxy.ProxyFilter;
import se.tink.integration.webdriver.service.proxy.ProxyRequest;
import se.tink.integration.webdriver.service.proxy.ProxyResponse;

public class WebDriverServiceModuleTest {

    private static final String EXAMPLE_DOMAIN = "google.com";
    private static final String EXAMPLE_URL = "https://" + EXAMPLE_DOMAIN;

    private WebDriverService driverService;

    @Before
    public void setup() {
        driverService =
                WebDriverServiceModule.createWebDriverService(mock(AgentTemporaryStorage.class));
    }

    @After
    public void clean() {
        ChromeDriverInitializer.quitChromeDriver(driverService);
        driverService.shutDownProxy();
    }

    @Test
    public void should_initialize_working_driver_with_proxy() {
        // given
        ExampleFilter filter = new ExampleFilter();
        driverService.registerProxyFilter("example_filter", filter);

        // when
        driverService.get(EXAMPLE_URL);

        // then
        assertThat(driverService.getCurrentUrl()).contains(EXAMPLE_DOMAIN);
        assertThat(filter.isHasRequest()).isTrue();
        assertThat(filter.isHasResponse()).isTrue();
    }

    @Getter
    private static class ExampleFilter implements ProxyFilter {

        private boolean hasRequest;
        private boolean hasResponse;

        @Override
        public void handleRequest(ProxyRequest request) {
            if (request.getMessageInfo().getUrl().contains(EXAMPLE_DOMAIN)) {
                hasRequest = true;
            }
        }

        @Override
        public void handleResponse(ProxyResponse response) {
            if (response.getMessageInfo().getUrl().contains(EXAMPLE_DOMAIN)) {
                hasResponse = true;
            }
        }
    };
}

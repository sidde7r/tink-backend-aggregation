package se.tink.integration.webdriver.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.service.proxy.ProxyResponseMatchers.ProxyResponseUrlSubstringMatcher;
import se.tink.integration.webdriver.service.proxy.ResponseFromProxy;

public class WebDriverServiceModuleIntegrationTest {

    private WebDriverService webDriver;

    @Before
    public void setup() {
        webDriver =
                WebDriverServiceModule.createWebDriverService(mock(AgentTemporaryStorage.class));
    }

    @After
    public void clean() {
        ChromeDriverInitializer.quitChromeDriver(webDriver);
        webDriver.shutDownProxy();
    }

    @Test
    public void should_initialize_working_driver_with_proxy() {
        // given
        webDriver.setProxyResponseMatcher(new ProxyResponseUrlSubstringMatcher("example.com"));

        // when
        webDriver.get("https://example.com/");
        Optional<ResponseFromProxy> responseFromProxy = webDriver.waitForMatchingProxyResponse(1);

        // then
        assertThat(webDriver.getCurrentUrl()).contains("https://example.com/");
        assertThat(responseFromProxy).isPresent();
    }
}

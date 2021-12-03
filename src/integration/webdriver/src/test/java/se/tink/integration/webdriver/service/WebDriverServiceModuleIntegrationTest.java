package se.tink.integration.webdriver.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.proxy.ResponseFromProxy;

public class WebDriverServiceModuleIntegrationTest {

    private WebDriverService webDriver;
    private ProxyManager proxyManager;

    @Before
    public void setup() {
        WebDriverModuleComponents webDriverModuleComponents =
                WebDriverModule.initializeModule(Mockito.mock(AgentTemporaryStorage.class));
        webDriver = webDriverModuleComponents.getWebDriver();
        proxyManager = webDriverModuleComponents.getProxyManager();
    }

    @After
    public void clean() {
        ChromeDriverInitializer.quitChromeDriver(webDriver);
        proxyManager.shutDownProxy();
    }

    @Test
    public void should_initialize_working_driver_with_proxy() {
        // given
        proxyManager.setUrlSubstringToListenFor("example.com");

        // when
        webDriver.get("https://example.com/");
        Optional<ResponseFromProxy> responseFromProxy = proxyManager.waitForProxyResponse(1);

        // then
        assertThat(webDriver.getCurrentUrl()).contains("https://example.com/");
        assertThat(responseFromProxy).isPresent();
    }
}

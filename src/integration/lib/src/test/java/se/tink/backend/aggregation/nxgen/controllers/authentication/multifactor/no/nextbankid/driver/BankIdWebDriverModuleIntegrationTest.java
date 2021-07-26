package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ProxyManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverInitializer;

public class BankIdWebDriverModuleIntegrationTest {

    private BankIdWebDriver webDriver;
    private ProxyManager proxyManager;

    @Before
    public void setup() {
        BankIdWebDriverModuleComponents webDriverModuleComponents =
                BankIdWebDriverModule.initializeModule(Mockito.mock(AgentTemporaryStorage.class));
        webDriver = webDriverModuleComponents.getWebDriver();
        proxyManager = webDriverModuleComponents.getProxyManager();
    }

    @After
    public void clean() {
        ChromeDriverInitializer.quitChromeDriver(webDriver.getDriver());
        proxyManager.shutDownProxy();
    }

    @Test
    public void should_initialize_working_driver_with_proxy() {
        // given
        proxyManager.setUrlSubstringToListenFor("example.com");

        // when
        webDriver.getUrl("https://example.com/");
        Optional<ResponseFromProxy> responseFromProxy = proxyManager.waitForProxyResponse(1);

        // then
        assertThat(webDriver.getCurrentUrl()).contains("https://example.com/");
        assertThat(responseFromProxy).isPresent();
    }
}

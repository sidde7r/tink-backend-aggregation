package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static se.tink.integration.webdriver.ProxyInitializer.toSeleniumProxy;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openqa.selenium.JavascriptExecutor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ProxyManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearcherImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.utils.Sleeper;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.ProxyInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;

public class BankIdWebDriverModule extends AbstractModule {

    /*
    Dependencies for module components
     */
    private final Sleeper sleeper;
    private final BrowserUpProxy proxy;
    private final WebDriverWrapper webDriver;
    private final JavascriptExecutor javascriptExecutor;

    private BankIdWebDriverModule(AgentTemporaryStorage agentTemporaryStorage) {
        sleeper = new Sleeper();
        proxy = ProxyInitializer.startProxyServer();
        webDriver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder().proxy(toSeleniumProxy(proxy)).build(),
                        agentTemporaryStorage);
        javascriptExecutor = webDriver;
    }

    /**
     * This is the only correct way of initializing {@link BankIdWebDriver} with all dependencies it
     * requires.
     */
    public static BankIdWebDriverModuleComponents initializeModule(
            AgentTemporaryStorage agentTemporaryStorage) {
        BankIdWebDriverModule driverModule = new BankIdWebDriverModule(agentTemporaryStorage);
        Injector injector = Guice.createInjector(driverModule);

        BankIdWebDriver bankIdWebDriver = injector.getInstance(BankIdWebDriver.class);
        ProxyManager proxyManager = injector.getInstance(ProxyManager.class);

        return new BankIdWebDriverModuleComponents(bankIdWebDriver, proxyManager);
    }

    @Override
    protected void configure() {
        bind(Sleeper.class).toInstance(sleeper);
        bind(BrowserUpProxy.class).toInstance(proxy);
        bind(WebDriverWrapper.class).toInstance(webDriver);
        bind(JavascriptExecutor.class).toInstance(javascriptExecutor);

        bind(BankIdWebDriver.class).to(BankIdWebDriverImpl.class);
        bind(BankIdElementsSearcher.class).to(BankIdElementsSearcherImpl.class);
    }
}

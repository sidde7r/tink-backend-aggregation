package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import static se.tink.integration.webdriver.ProxyInitializer.toSeleniumProxy;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openqa.selenium.WebDriver;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.ProxyInitializer;
import se.tink.integration.webdriver.utils.Sleeper;
import se.tink.integration.webdriver.utils.WebDriverWrapper;
import se.tink.integration.webdriver.utils.WebDriverWrapperImpl;

public class BankIdWebDriverModule extends AbstractModule {

    /*
    Dependencies for module components
     */
    private final Sleeper sleeper;
    private final BrowserUpProxy proxy;
    private final WebDriver webDriver;
    private final WebDriverWrapper driverWrapper;

    private BankIdWebDriverModule() {
        sleeper = new Sleeper();
        proxy = ProxyInitializer.startProxyServer();
        webDriver = ChromeDriverInitializer.constructChromeDriver(toSeleniumProxy(proxy));
        driverWrapper = new WebDriverWrapperImpl(webDriver, sleeper);
    }

    /**
     * This is the only correct way of initializing {@link BankIdWebDriver} with all dependencies it
     * requires.
     */
    public static BankIdWebDriverModuleComponents initializeModule() {
        BankIdWebDriverModule driverModule = new BankIdWebDriverModule();
        Injector injector = Guice.createInjector(driverModule);

        BankIdWebDriver bankIdWebDriver = injector.getInstance(BankIdWebDriver.class);
        ProxyManager proxyManager = injector.getInstance(ProxyManager.class);

        return new BankIdWebDriverModuleComponents(bankIdWebDriver, proxyManager);
    }

    @Override
    protected void configure() {
        bind(Sleeper.class).toInstance(sleeper);
        bind(BrowserUpProxy.class).toInstance(proxy);
        bind(WebDriver.class).toInstance(webDriver);
        bind(WebDriverWrapper.class).toInstance(driverWrapper);
    }
}

package se.tink.integration.webdriver.service;

import static se.tink.integration.webdriver.ProxyInitializer.toSeleniumProxy;

import com.browserup.bup.BrowserUpProxy;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openqa.selenium.JavascriptExecutor;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.ChromeDriverConfig;
import se.tink.integration.webdriver.ChromeDriverInitializer;
import se.tink.integration.webdriver.ProxyInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.service.basicutils.Sleeper;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtils;
import se.tink.integration.webdriver.service.basicutils.WebDriverBasicUtilsImpl;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcher;
import se.tink.integration.webdriver.service.searchelements.ElementsSearcherImpl;

public class WebDriverModule extends AbstractModule {

    /*
    Dependencies for module components
     */
    private final Sleeper sleeper;
    private final BrowserUpProxy proxy;
    private final WebDriverWrapper webDriver;
    private final JavascriptExecutor javascriptExecutor;

    private WebDriverModule(AgentTemporaryStorage agentTemporaryStorage) {
        sleeper = new Sleeper();
        proxy = ProxyInitializer.startProxyServer();
        webDriver =
                ChromeDriverInitializer.constructChromeDriver(
                        ChromeDriverConfig.builder().proxy(toSeleniumProxy(proxy)).build(),
                        agentTemporaryStorage);
        javascriptExecutor = webDriver;
    }

    /**
     * This is the only correct way of initializing {@link WebDriverService} with all dependencies
     * it requires.
     */
    public static WebDriverModuleComponents initializeModule(
            AgentTemporaryStorage agentTemporaryStorage) {
        WebDriverModule driverModule = new WebDriverModule(agentTemporaryStorage);
        Injector injector = Guice.createInjector(driverModule);

        WebDriverService webDriverService = injector.getInstance(WebDriverService.class);
        ProxyManager proxyManager = injector.getInstance(ProxyManager.class);

        return new WebDriverModuleComponents(webDriverService, proxyManager);
    }

    @Override
    protected void configure() {
        bind(Sleeper.class).toInstance(sleeper);
        bind(BrowserUpProxy.class).toInstance(proxy);
        bind(WebDriverWrapper.class).toInstance(webDriver);
        bind(JavascriptExecutor.class).toInstance(javascriptExecutor);

        bind(WebDriverBasicUtils.class).to(WebDriverBasicUtilsImpl.class);
        bind(ElementsSearcher.class).to(ElementsSearcherImpl.class);
        bind(WebDriverService.class).to(WebDriverServiceImpl.class);
    }
}

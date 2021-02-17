package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.PHANTOMJS_TIMEOUT_SECONDS;

import com.google.inject.AbstractModule;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.Sleeper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class NemIdSSIFrameModule extends AbstractModule {

    /*
    External dependencies
    */
    private final NemIdParametersFetcher nemIdParametersFetcher;
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;
    private final MetricContext metricContext;

    /*
    Module dependencies
     */
    private NemIdMetrics metrics;
    private WebDriver webDriver;
    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater credentialsStatusUpdater;
    private NemIdTokenValidator tokenValidator;

    public NemIdSSIFrameModule(
            NemIdParametersFetcher nemIdParametersFetcher,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext) {
        this.nemIdParametersFetcher = nemIdParametersFetcher;
        this.catalog = catalog;
        this.statusUpdater = statusUpdater;
        this.supplementalInformationController = supplementalInformationController;
        this.metricContext = metricContext;

        setUpModuleDependencies();
    }

    @Override
    protected void configure() {
        bind(NemIdParametersFetcher.class).toInstance(nemIdParametersFetcher);
        bind(Catalog.class).toInstance(catalog);
        bind(StatusUpdater.class).toInstance(statusUpdater);
        bind(SupplementalInformationController.class).toInstance(supplementalInformationController);
        bind(MetricContext.class).toInstance(metricContext);

        bind(NemIdMetrics.class).toInstance(metrics);
        bind(WebDriver.class).toInstance(webDriver);
        bind(NemIdWebDriverWrapper.class).toInstance(driverWrapper);
        bind(NemIdCredentialsStatusUpdater.class).toInstance(credentialsStatusUpdater);
        bind(NemIdTokenValidator.class).toInstance(tokenValidator);
    }

    private void setUpModuleDependencies() {
        metrics = new NemIdMetrics(metricContext.getMetricRegistry());

        webDriver =
                metrics.executeWithTimer(
                        NemIdSSIFrameModule::constructWebDriver,
                        NemIdMetricLabel.WEB_DRIVER_CONSTRUCTION);
        driverWrapper = new NemIdWebDriverWrapper(webDriver, new Sleeper());

        credentialsStatusUpdater = new NemIdCredentialsStatusUpdater(statusUpdater, catalog);

        tokenValidator = new NemIdTokenValidator(new NemIdTokenParser());
    }

    private static WebDriver constructWebDriver() {
        WebDriver driver = getPhantomJsDriver();
        driver.manage().timeouts().pageLoadTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return driver;
    }

    private static WebDriver getPhantomJsDriver() {
        File file = readDriverFile();

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, file.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs =
                new String[] {
                    // To allow iframe-hacking
                    "--web-security=false",
                    // No need to load images
                    "--load-images=false",
                    // For debugging, activate these:
                    // "--webdriver-loglevel=DEBUG",
                    "--debug=false",
                    // "--proxy=127.0.0.1:8888",
                    // "--ignore-ssl-errors=true",
                    // "--webdriver-logfile=/tmp/phantomjs.log"
                };

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                NemIdConstants.USER_AGENT);

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                NemIdConstants.USER_AGENT);

        return new PhantomJSDriver(capabilities);
    }

    private static File readDriverFile() {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            return new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            return new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }
}

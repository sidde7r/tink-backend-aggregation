package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import com.google.inject.AbstractModule;
import java.util.concurrent.Callable;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.WebDriverInitializer;
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
                        (Callable<WebDriver>) WebDriverInitializer::constructWebDriver,
                        NemIdMetricLabel.WEB_DRIVER_CONSTRUCTION);
        driverWrapper = new NemIdWebDriverWrapper(webDriver, new Sleeper());

        credentialsStatusUpdater = new NemIdCredentialsStatusUpdater(statusUpdater, catalog);

        tokenValidator = new NemIdTokenValidator(new NemIdTokenParser());
    }
}

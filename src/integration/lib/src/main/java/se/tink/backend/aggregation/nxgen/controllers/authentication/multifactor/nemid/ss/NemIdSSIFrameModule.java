package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.Sleeper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.PhantomJsConfig;
import se.tink.integration.webdriver.PhantomJsInitializer;
import se.tink.integration.webdriver.WebDriverWrapper;
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
    private final AgentTemporaryStorage agentTemporaryStorage;

    /*
    Module dependencies
     */
    private NemIdMetrics metrics;
    private WebDriverWrapper webDriver;
    private NemIdWebDriverWrapper driverWrapper;
    private NemIdCredentialsStatusUpdater credentialsStatusUpdater;
    private NemIdTokenValidator tokenValidator;

    public NemIdSSIFrameModule(
            NemIdParametersFetcher nemIdParametersFetcher,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext,
            AgentTemporaryStorage agentTemporaryStorage) {
        this.nemIdParametersFetcher = nemIdParametersFetcher;
        this.catalog = catalog;
        this.statusUpdater = statusUpdater;
        this.supplementalInformationController = supplementalInformationController;
        this.metricContext = metricContext;
        this.agentTemporaryStorage = agentTemporaryStorage;

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
        bind(WebDriverWrapper.class).toInstance(webDriver);
        bind(NemIdWebDriverWrapper.class).toInstance(driverWrapper);
        bind(NemIdCredentialsStatusUpdater.class).toInstance(credentialsStatusUpdater);
        bind(NemIdTokenValidator.class).toInstance(tokenValidator);
        bind(AgentTemporaryStorage.class).toInstance(agentTemporaryStorage);
    }

    private void setUpModuleDependencies() {
        metrics = new NemIdMetrics(metricContext.getMetricRegistry());

        webDriver =
                metrics.executeWithTimer(
                        () ->
                                PhantomJsInitializer.constructWebDriver(
                                        PhantomJsConfig.defaultConfig(), agentTemporaryStorage),
                        NemIdMetricLabel.WEB_DRIVER_CONSTRUCTION);
        driverWrapper = new NemIdWebDriverWrapper(webDriver, new Sleeper());

        credentialsStatusUpdater = new NemIdCredentialsStatusUpdater(statusUpdater, catalog);

        tokenValidator = new NemIdTokenValidator(new NemIdTokenParser());
    }
}

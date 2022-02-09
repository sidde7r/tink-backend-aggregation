package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
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
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
public class NemIdSSIFrameModule extends AbstractModule {

    /*
    Dependencies for module components
    */
    private final NemIdParametersFetcher nemIdParametersFetcher;
    private final NemIdCredentialsProvider credentialsProvider;
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;
    private final MetricContext metricContext;
    private final AgentTemporaryStorage agentTemporaryStorage;

    private final NemIdMetrics metrics;
    private final WebDriverWrapper webDriver;
    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdCredentialsStatusUpdater credentialsStatusUpdater;
    private final NemIdTokenValidator tokenValidator;

    public static NemIdSSIFrameModule initializeModule(
            NemIdParametersFetcher nemIdParametersFetcher,
            NemIdCredentialsProvider credentialsProvider,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext,
            AgentTemporaryStorage agentTemporaryStorage) {

        NemIdMetrics metrics = new NemIdMetrics(metricContext.getMetricRegistry());

        WebDriverWrapper webDriver =
                metrics.executeWithTimer(
                        () ->
                                PhantomJsInitializer.constructWebDriver(
                                        PhantomJsConfig.defaultConfig(), agentTemporaryStorage),
                        NemIdMetricLabel.WEB_DRIVER_CONSTRUCTION);
        NemIdWebDriverWrapper driverWrapper = new NemIdWebDriverWrapper(webDriver, new Sleeper());

        NemIdCredentialsStatusUpdater credentialsStatusUpdater =
                new NemIdCredentialsStatusUpdater(statusUpdater, catalog);

        NemIdTokenValidator tokenValidator = new NemIdTokenValidator(new NemIdTokenParser());

        return new NemIdSSIFrameModule(
                nemIdParametersFetcher,
                credentialsProvider,
                catalog,
                statusUpdater,
                supplementalInformationController,
                metricContext,
                agentTemporaryStorage,
                metrics,
                webDriver,
                driverWrapper,
                credentialsStatusUpdater,
                tokenValidator);
    }

    @Override
    protected void configure() {
        bind(NemIdParametersFetcher.class).toInstance(nemIdParametersFetcher);
        bind(NemIdCredentialsProvider.class).toInstance(credentialsProvider);
        bind(Catalog.class).toInstance(catalog);
        bind(StatusUpdater.class).toInstance(statusUpdater);
        bind(SupplementalInformationController.class).toInstance(supplementalInformationController);
        bind(MetricContext.class).toInstance(metricContext);
        bind(AgentTemporaryStorage.class).toInstance(agentTemporaryStorage);

        bind(NemIdMetrics.class).toInstance(metrics);
        bind(WebDriverWrapper.class).toInstance(webDriver);
        bind(NemIdWebDriverWrapper.class).toInstance(driverWrapper);
        bind(NemIdCredentialsStatusUpdater.class).toInstance(credentialsStatusUpdater);
        bind(NemIdTokenValidator.class).toInstance(tokenValidator);
    }
}

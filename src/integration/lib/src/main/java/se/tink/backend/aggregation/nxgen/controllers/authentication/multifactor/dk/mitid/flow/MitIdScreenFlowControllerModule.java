package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.MitIdCodeAppPollingProxyFilter;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;
import se.tink.libraries.i18n_aggregation.Catalog;

@RequiredArgsConstructor
public class MitIdScreenFlowControllerModule extends AbstractModule {

    /*
    Dependencies for module components
     */
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;
    private final WebDriverService driverService;
    private final MitIdAuthenticator mitIdAuthenticator;

    @Override
    protected void configure() {
        bind(Catalog.class).toInstance(catalog);
        bind(StatusUpdater.class).toInstance(statusUpdater);
        bind(SupplementalInformationController.class).toInstance(supplementalInformationController);
        bind(WebDriverService.class).toInstance(driverService);
        bind(MitIdLocatorsElements.class).toInstance(mitIdAuthenticator.getLocatorsElements());
        bind(MitIdAuthenticator.class).toInstance(mitIdAuthenticator);

        ProxySaveResponseFilter proxySaveResponseFilter =
                new ProxySaveResponseFilter(
                        mitIdAuthenticator.getMatcherForAuthenticationFinishResponse());
        bind(ProxySaveResponseFilter.class).toInstance(proxySaveResponseFilter);

        MitIdCodeAppPollingProxyFilter codeAppPollingProxyFilter =
                new MitIdCodeAppPollingProxyFilter();
        bind(MitIdCodeAppPollingProxyFilter.class).toInstance(codeAppPollingProxyFilter);
    }

    /**
     * This is the only correct way of initializing {@link MitIdScreenFlowController} with all
     * dependencies it requires.
     */
    public static MitIdScreenFlowController createMitIdScreenFlowController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            WebDriverService driverService,
            MitIdAuthenticator authenticator) {

        MitIdScreenFlowControllerModule module =
                new MitIdScreenFlowControllerModule(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        driverService,
                        authenticator);
        Injector injector = Guice.createInjector(module);
        return injector.getInstance(MitIdScreenFlowController.class);
    }
}

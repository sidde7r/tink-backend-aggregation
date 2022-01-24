package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.MitIdCodeAppPollingProxyListener;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class MitIdFlowControllerModule extends AbstractModule {

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
        bind(MitIdLocators.class).toInstance(mitIdAuthenticator.getLocators());
        bind(MitIdAuthenticator.class).toInstance(mitIdAuthenticator);

        MitIdAuthFinishProxyListener proxySaveResponseListener =
                new MitIdAuthFinishProxyListener(
                        mitIdAuthenticator.getMatcherForAuthenticationFinishResponse());
        bind(MitIdAuthFinishProxyListener.class).toInstance(proxySaveResponseListener);

        MitIdCodeAppPollingProxyListener codeAppPollingProxyListener =
                new MitIdCodeAppPollingProxyListener();
        bind(MitIdCodeAppPollingProxyListener.class).toInstance(codeAppPollingProxyListener);
    }

    /**
     * This is the only correct way of initializing {@link MitIdFlowController} with all
     * dependencies it requires.
     */
    public static MitIdFlowController createMitIdFlowController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            WebDriverService driverService,
            MitIdAuthenticator authenticator) {

        MitIdFlowControllerModule module =
                new MitIdFlowControllerModule(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        driverService,
                        authenticator);
        Injector injector = Guice.createInjector(module);
        return injector.getInstance(MitIdFlowController.class);
    }
}

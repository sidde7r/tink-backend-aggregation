package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class BankIdIframeModule extends AbstractModule {

    /*
    Dependencies for module components
     */
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;
    private final SupplementalInformationController supplementalInformationController;
    private final BankIdWebDriver bankIdWebDriver;
    private final BankIdAuthenticationState authenticationState;

    @Override
    protected void configure() {
        bind(Catalog.class).toInstance(catalog);
        bind(StatusUpdater.class).toInstance(statusUpdater);
        bind(SupplementalInformationController.class).toInstance(supplementalInformationController);
        bind(BankIdWebDriver.class).toInstance(bankIdWebDriver);
        bind(BankIdAuthenticationState.class).toInstance(authenticationState);
    }

    /**
     * This is the only correct way of initializing {@link BankIdIframeController} with all
     * dependencies it requires.
     */
    public static BankIdIframeController initializeIframeController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            BankIdWebDriver bankIdWebDriver,
            BankIdAuthenticationState authenticationState) {

        BankIdIframeModule bankIdModule =
                new BankIdIframeModule(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        bankIdWebDriver,
                        authenticationState);
        Injector injector = Guice.createInjector(bankIdModule);
        return injector.getInstance(BankIdIframeController.class);
    }
}

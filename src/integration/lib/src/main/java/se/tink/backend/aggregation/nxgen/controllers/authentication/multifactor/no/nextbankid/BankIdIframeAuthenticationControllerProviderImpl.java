package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeModule;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.WebDriverServiceModule;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n_aggregation.Catalog;

public class BankIdIframeAuthenticationControllerProviderImpl
        implements BankIdIframeAuthenticationControllerProvider {

    public BankIdIframeAuthenticationController createAuthController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            BankIdIframeInitializer iframeInitializer,
            BankIdIframeAuthenticator iframeAuthenticator,
            UserAvailability userAvailability,
            AgentTemporaryStorage agentTemporaryStorage) {

        WebDriverService webDriverService =
                WebDriverServiceModule.createWebDriverService(agentTemporaryStorage);

        ProxySaveResponseFilter authFinishProxyFilter =
                new ProxySaveResponseFilter(
                        iframeAuthenticator
                                .getProxyResponseMatcherToDetectAuthenticationWasFinished());

        BankIdAuthenticationState authenticationState = new BankIdAuthenticationState();

        BankIdIframeController iframeController =
                BankIdIframeModule.initializeIframeController(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        webDriverService,
                        authenticationState);

        return new BankIdIframeAuthenticationController(
                webDriverService,
                agentTemporaryStorage,
                authenticationState,
                iframeInitializer,
                iframeAuthenticator,
                authFinishProxyFilter,
                iframeController,
                userAvailability);
    }
}

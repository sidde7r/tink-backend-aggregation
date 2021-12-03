package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeModule;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverModule;
import se.tink.integration.webdriver.service.WebDriverModuleComponents;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n.Catalog;

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

        WebDriverModuleComponents webDriverModuleComponents =
                WebDriverModule.initializeModule(agentTemporaryStorage);
        WebDriverService webDriver = webDriverModuleComponents.getWebDriver();
        ProxyManager proxyManager = webDriverModuleComponents.getProxyManager();

        BankIdAuthenticationState authenticationState = new BankIdAuthenticationState();

        BankIdIframeController iframeController =
                BankIdIframeModule.initializeIframeController(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        webDriver,
                        authenticationState);

        return new BankIdIframeAuthenticationController(
                webDriver,
                agentTemporaryStorage,
                proxyManager,
                authenticationState,
                iframeInitializer,
                iframeAuthenticator,
                iframeController,
                userAvailability);
    }
}

package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverModule;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverModuleComponents;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ProxyManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeModule;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class BankIdIframeAuthenticationControllerProviderImpl
        implements BankIdIframeAuthenticationControllerProvider {

    public BankIdIframeAuthenticationController createAuthController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            BankIdIframeInitializer iframeInitializer,
            BankIdIframeAuthenticator iframeAuthenticator) {

        BankIdWebDriverModuleComponents webDriverModuleComponents =
                BankIdWebDriverModule.initializeModule();
        BankIdWebDriver webDriver = webDriverModuleComponents.getWebDriver();
        ProxyManager proxyManager = webDriverModuleComponents.getProxyManager();

        BankIdIframeController iframeController =
                BankIdIframeModule.initializeIframeController(
                        catalog, statusUpdater, supplementalInformationController, webDriver);

        return new BankIdIframeAuthenticationController(
                webDriver, proxyManager, iframeInitializer, iframeAuthenticator, iframeController);
    }
}

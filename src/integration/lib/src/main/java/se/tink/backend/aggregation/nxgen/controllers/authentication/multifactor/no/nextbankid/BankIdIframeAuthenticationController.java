package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverModule;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriverModuleComponents;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.ProxyManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.ResponseFromProxy;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeModule;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BankIdIframeAuthenticationController
        implements Authenticator, MultiFactorAuthenticator, AutoAuthenticator {

    private static final int WAIT_FOR_PROXY_RESPONSE_IN_SECONDS = 10;

    private final BankIdWebDriver webDriver;
    private final ProxyManager proxyManager;
    private final BankIdIframeInitializer iframeInitializer;
    private final BankIdIframeAuthenticator iframeAuthenticator;
    private final BankIdIframeController iframeController;

    /**
     * This is the only correct way to initialize {@link BankIdIframeAuthenticationController} with
     * all it's dependencies.
     */
    public static BankIdIframeAuthenticationController authenticationController(
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

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate() {
        iframeAuthenticator.autoAuthenticate();
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        try {
            listenForProxyResponseFromAuthFinishUrl();

            BankIdIframeFirstStep iframeFirstStep = iframeInitializer.initializeIframe(webDriver);
            iframeController.authenticateWithCredentials(credentials, iframeFirstStep);

            ResponseFromProxy authFinishUrlProxyResponse = waitForAuthFinishUrlResponse();

            iframeAuthenticator.handleBankIdAuthenticationResult(
                    BankIdIframeAuthenticationResult.builder()
                            .proxyResponseFromAuthFinishUrl(authFinishUrlProxyResponse)
                            .webDriver(webDriver)
                            .build());

        } catch (RuntimeException e) {
            log.error(
                    "{} BankID iframe authentication error: {}\n{}",
                    e.getMessage(),
                    webDriver.getFullPageSourceLog(),
                    e);
            throw e;

        } finally {
            proxyManager.shutDownProxy();
            webDriver.quitDriver();
        }
    }

    private void listenForProxyResponseFromAuthFinishUrl() {
        String urlToListenFor =
                iframeAuthenticator.getSubstringOfUrlIndicatingAuthenticationFinish();
        proxyManager.listenForProxyResponseByResponseUrlSubstring(urlToListenFor);
    }

    private ResponseFromProxy waitForAuthFinishUrlResponse() {
        return proxyManager
                .waitForProxyResponse(WAIT_FOR_PROXY_RESPONSE_IN_SECONDS)
                .orElseThrow(() -> new IllegalStateException("Did not found proxy response"));
    }
}

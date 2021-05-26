package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ProxyManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;

@RunWith(JUnitParamsRunner.class)
public class BankIdIframeAuthenticationControllerTest {

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;
    private ProxyManager proxyManager;
    private BankIdAuthenticationState authenticationState;
    private BankIdIframeInitializer iframeInitializer;
    private BankIdIframeAuthenticator iframeAuthenticator;
    private BankIdIframeController iframeController;

    private Credentials credentials;
    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdIframeAuthenticationController authenticationController;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);
        proxyManager = mock(ProxyManager.class);
        authenticationState = mock(BankIdAuthenticationState.class);
        iframeInitializer = mock(BankIdIframeInitializer.class);
        iframeAuthenticator = mock(BankIdIframeAuthenticator.class);
        iframeController = mock(BankIdIframeController.class);
        credentials = mock(Credentials.class);

        mocksToVerifyInOrder =
                inOrder(
                        webDriver,
                        proxyManager,
                        authenticationState,
                        iframeInitializer,
                        iframeAuthenticator,
                        iframeController);

        authenticationController =
                new BankIdIframeAuthenticationController(
                        webDriver,
                        proxyManager,
                        authenticationState,
                        iframeInitializer,
                        iframeAuthenticator,
                        iframeController);
    }

    @Test
    @Parameters(method = "allFirstIframeWindows")
    public void should_run_manual_authentication(BankIdIframeFirstWindow firstWindow) {
        // given
        when(iframeInitializer.initializeIframe(webDriver)).thenReturn(firstWindow);

        when(iframeAuthenticator.getSubstringOfUrlIndicatingAuthenticationFinish())
                .thenReturn("part.of.some.url");

        ResponseFromProxy responseFromProxy = mock(ResponseFromProxy.class);
        when(proxyManager.waitForProxyResponse(anyInt()))
                .thenReturn(Optional.of(responseFromProxy));

        // when
        authenticationController.authenticate(credentials);

        // then
        verifyStartsListeningForResponseFromUrl("part.of.some.url");
        verifyIframeInitialization();
        verifySavesFirstIframeWindow(firstWindow);
        verifyAuthenticationWithIframeController();
        verifyWaitsForResponseFromUrl(10);
        verifyHandlesIframeAuthResult(
                BankIdIframeAuthenticationResult.builder()
                        .proxyResponseFromAuthFinishUrl(responseFromProxy)
                        .webDriver(webDriver)
                        .build());

        mocksToVerifyInOrder.verify(proxyManager).shutDownProxy();
        mocksToVerifyInOrder.verify(webDriver).quitDriver();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] allFirstIframeWindows() {
        return BankIdIframeFirstWindow.values();
    }

    private void verifySavesFirstIframeWindow(BankIdIframeFirstWindow firstWindow) {
        mocksToVerifyInOrder.verify(authenticationState).setFirstIframeWindow(firstWindow);
    }

    private void verifyIframeInitialization() {
        mocksToVerifyInOrder.verify(iframeInitializer).initializeIframe(webDriver);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyStartsListeningForResponseFromUrl(String url) {
        mocksToVerifyInOrder.verify(proxyManager).setUrlSubstringToListenFor(url);
    }

    private void verifyAuthenticationWithIframeController() {
        mocksToVerifyInOrder.verify(iframeController).authenticateWithCredentials(credentials);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyWaitsForResponseFromUrl(int waitForSeconds) {
        mocksToVerifyInOrder.verify(proxyManager).waitForProxyResponse(waitForSeconds);
    }

    private void verifyHandlesIframeAuthResult(BankIdIframeAuthenticationResult result) {
        mocksToVerifyInOrder.verify(iframeAuthenticator).handleBankIdAuthenticationResult(result);
    }
}

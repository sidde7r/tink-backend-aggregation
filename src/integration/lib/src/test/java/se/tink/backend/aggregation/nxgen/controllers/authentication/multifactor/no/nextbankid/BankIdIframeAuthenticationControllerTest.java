package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxyResponseMatcher;
import se.tink.integration.webdriver.service.proxy.ResponseFromProxy;
import se.tink.libraries.credentials.service.UserAvailability;

@RunWith(JUnitParamsRunner.class)
public class BankIdIframeAuthenticationControllerTest {

    /*
    Mocks
     */
    private WebDriverService webDriver;
    private AgentTemporaryStorage agentTemporaryStorage;
    private BankIdAuthenticationState authenticationState;
    private BankIdIframeInitializer iframeInitializer;
    private BankIdIframeAuthenticator iframeAuthenticator;
    private BankIdIframeController iframeController;

    private Credentials credentials;
    private UserAvailability userAvailability;
    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdIframeAuthenticationController authenticationController;

    @Before
    public void setup() {
        webDriver = mock(WebDriverService.class);
        when(webDriver.getDriverId()).thenReturn("SAMPLE_DRIVER_ID");
        agentTemporaryStorage = mock(AgentTemporaryStorage.class);
        authenticationState = mock(BankIdAuthenticationState.class);
        iframeInitializer = mock(BankIdIframeInitializer.class);
        iframeAuthenticator = mock(BankIdIframeAuthenticator.class);
        iframeController = mock(BankIdIframeController.class);
        credentials = mock(Credentials.class);
        userAvailability = mock(UserAvailability.class);

        when(userAvailability.isUserPresent()).thenReturn(true);

        mocksToVerifyInOrder =
                inOrder(
                        webDriver,
                        agentTemporaryStorage,
                        authenticationState,
                        iframeInitializer,
                        iframeAuthenticator,
                        iframeController);

        authenticationController =
                new BankIdIframeAuthenticationController(
                        webDriver,
                        agentTemporaryStorage,
                        authenticationState,
                        iframeInitializer,
                        iframeAuthenticator,
                        iframeController,
                        userAvailability);
    }

    @Test
    public void should_throw_exception_when_user_is_not_present() {
        // given
        when(userAvailability.isUserPresent()).thenReturn(false);

        // when
        Throwable result = catchThrowable(() -> authenticationController.authenticate(credentials));

        // then
        assertThat(result).isInstanceOf(SessionException.class);
        assertThat(result)
                .hasMessage(
                        "User is not present. Fail refresh before entering user's data into BankID");
    }

    @Test
    @Parameters(method = "allFirstIframeWindows")
    public void should_run_manual_authentication(BankIdIframeFirstWindow firstWindow) {
        // given
        when(iframeInitializer.initializeIframe(webDriver)).thenReturn(firstWindow);

        ProxyResponseMatcher proxyResponseMatcher = mock(ProxyResponseMatcher.class);
        when(iframeAuthenticator.getMatcherForResponseThatIndicatesAuthenticationWasFinished())
                .thenReturn(proxyResponseMatcher);

        ResponseFromProxy responseFromProxy = mock(ResponseFromProxy.class);
        when(webDriver.waitForMatchingProxyResponse(anyInt()))
                .thenReturn(Optional.of(responseFromProxy));

        // when
        authenticationController.authenticate(credentials);

        // then
        verifyStartsListeningForResponseMatchingMatcher(proxyResponseMatcher);
        verifyIframeInitialization();
        verifySavesFirstIframeWindow(firstWindow);
        verifyAuthenticationWithIframeController();
        verifyWaitsForResponseFromUrl(10);
        verifyHandlesIframeAuthResult(
                BankIdIframeAuthenticationResult.builder()
                        .proxyResponseFromAuthFinishUrl(responseFromProxy)
                        .webDriver(webDriver)
                        .build());

        mocksToVerifyInOrder.verify(webDriver).shutDownProxy();
        mocksToVerifyInOrder.verify(agentTemporaryStorage).remove("SAMPLE_DRIVER_ID");
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
    private void verifyStartsListeningForResponseMatchingMatcher(
            ProxyResponseMatcher proxyResponseMatcher) {
        mocksToVerifyInOrder.verify(webDriver).setProxyResponseMatcher(proxyResponseMatcher);
    }

    private void verifyAuthenticationWithIframeController() {
        mocksToVerifyInOrder.verify(iframeController).authenticateWithCredentials(credentials);
    }

    @SuppressWarnings("SameParameterValue")
    private void verifyWaitsForResponseFromUrl(int waitForSeconds) {
        mocksToVerifyInOrder.verify(webDriver).waitForMatchingProxyResponse(waitForSeconds);
    }

    private void verifyHandlesIframeAuthResult(BankIdIframeAuthenticationResult result) {
        mocksToVerifyInOrder.verify(iframeAuthenticator).handleBankIdAuthenticationResult(result);
    }
}

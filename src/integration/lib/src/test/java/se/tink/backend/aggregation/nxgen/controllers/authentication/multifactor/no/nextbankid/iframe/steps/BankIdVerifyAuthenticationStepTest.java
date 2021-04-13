package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_PRIVATE_PASSWORD_ERROR_BUBBLE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.WAIT_FOR_SIGN_THAT_AUTHENTICATION_IS_FINISHED_FOR_SECONDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorDoesNotExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyNTimes;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;

@RunWith(JUnitParamsRunner.class)
public class BankIdVerifyAuthenticationStepTest {

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;
    private BankIdScreensManager screensManager;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdVerifyAuthenticationStep verifyAuthenticationStep;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);
        screensManager = mock(BankIdScreensManager.class);

        mocksToVerifyInOrder = inOrder(webDriver, screensManager);

        verifyAuthenticationStep = new BankIdVerifyAuthenticationStep(webDriver, screensManager);
    }

    @Test
    public void should_recognize_authentication_has_finished_when_iframe_element_doesnt_exist() {
        // given
        mockLocatorDoesNotExists(LOC_IFRAME, webDriver);

        // when
        verifyAuthenticationStep.verify();

        // then
        verifyChecksIfControllerLeftIframe();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_recognize_failed_authentication_caused_by_invalid_password_error() {
        // given
        mockLocatorExists(LOC_IFRAME, webDriver);
        mockLocatorExists(LOC_PRIVATE_PASSWORD_ERROR_BUBBLE, webDriver);

        // when
        Throwable throwable = catchThrowable(() -> verifyAuthenticationStep.verify());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, BankIdNOError.INVALID_BANK_ID_PASSWORD.exception());

        verifyChecksIfControllerLeftIframe();
        verifyChecksForPasswordErrorBubble();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_recognize_failed_authentication_caused_by_error_screen() {
        // given
        mockLocatorExists(LOC_IFRAME, webDriver);
        mockLocatorDoesNotExists(LOC_PRIVATE_PASSWORD_ERROR_BUBBLE, webDriver);

        RuntimeException errorScreenFoundException = mock(RuntimeException.class);
        when(screensManager.tryWaitForAnyScreenFromQuery(any()))
                .thenThrow(errorScreenFoundException);

        // when
        Throwable throwable = catchThrowable(() -> verifyAuthenticationStep.verify());

        // then
        assertThat(throwable).isEqualTo(errorScreenFoundException);

        verifyChecksIfControllerLeftIframe();
        verifyChecksForPasswordErrorBubble();
        verifyChecksForErrorScreens();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "allScreens")
    public void
            should_check_all_possible_signs_of_authentication_failure_for_n_seconds_and_then_throw_general_error_with_info_about_current_screen(
                    BankIdScreen currentScreen) {
        // given
        mockLocatorExists(LOC_IFRAME, webDriver);
        mockLocatorDoesNotExists(LOC_PRIVATE_PASSWORD_ERROR_BUBBLE, webDriver);

        when(screensManager.tryWaitForAnyScreenFromQuery(any())).thenReturn(Optional.empty());

        when(screensManager.waitForAnyScreenFromQuery(any())).thenReturn(currentScreen);

        // when
        Throwable throwable = catchThrowable(() -> verifyAuthenticationStep.verify());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        String.format(
                                "%s Authentication did not finish. Current screen: %s",
                                BANK_ID_LOG_PREFIX, currentScreen));

        verifyNTimes(
                () -> {
                    verifyChecksIfControllerLeftIframe();
                    verifyChecksForPasswordErrorBubble();
                    verifyChecksForErrorScreens();
                    mocksToVerifyInOrder.verify(webDriver).sleepFor(1_000);
                },
                WAIT_FOR_SIGN_THAT_AUTHENTICATION_IS_FINISHED_FOR_SECONDS);

        verifyTriesToDetectAnyScreen();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] allScreens() {
        return BankIdScreen.getAllScreens().toArray();
    }

    private void verifyChecksIfControllerLeftIframe() {
        mocksToVerifyInOrder
                .verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_IFRAME)
                                .searchOnlyOnce()
                                .build());
    }

    private void verifyChecksForPasswordErrorBubble() {
        mocksToVerifyInOrder
                .verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_PRIVATE_PASSWORD_ERROR_BUBBLE)
                                .searchForSeconds(2)
                                .build());
    }

    private void verifyChecksForErrorScreens() {
        mocksToVerifyInOrder
                .verify(screensManager)
                .tryWaitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(emptyList())
                                .waitForSeconds(0)
                                .verifyNoErrorScreens(true)
                                .build());
    }

    private void verifyTriesToDetectAnyScreen() {
        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.getAllScreens())
                                .build());
    }
}

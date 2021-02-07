package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyNTimes;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMockWithText;

import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@RunWith(JUnitParamsRunner.class)
public class NemIdCodeCardGetTokenStepTest {

    private static final String CODE_CARD_CODE = "SAMPLE CODE CARD CODE";

    private NemIdWebDriverWrapper driverWrapper;

    private NemIdCodeCardGetTokenStep getTokenStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setUp() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);

        getTokenStep = new NemIdCodeCardGetTokenStep(driverWrapper, nemIdMetricsMock());

        mocksToVerifyInOrder = Mockito.inOrder(driverWrapper);
    }

    @Test
    public void should_submit_code_from_user_and_read_nem_id_token() {
        // given
        mockThereIsWebElementWithText(NEMID_TOKEN, "--- SAMPLE TOKEN ---");

        // when
        String nemIdToken = getTokenStep.enterCodeAndGetToken(CODE_CARD_CODE);

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_look_for_not_empty_token_and_not_empty_error_message() {
        // given
        when(driverWrapper.tryFindElement(NEMID_TOKEN))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(webElementMockWithText("")))
                .thenReturn(Optional.of(webElementMockWithText("--- SAMPLE TOKEN 123 ---")));
        mockThereIsNoSuchElement(NOT_EMPTY_ERROR_MESSAGE);

        // when
        String nemIdToken = getTokenStep.enterCodeAndGetToken(CODE_CARD_CODE);

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN 123 ---");

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
                    mocksToVerifyInOrder
                            .verify(driverWrapper)
                            .tryFindElement(NOT_EMPTY_ERROR_MESSAGE);
                },
                2);
        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters({"Fejl i nøgle", "---fEJL i nøGLE---", " Fejl i nøgle "})
    public void should_throw_invalid_code_card_code_error_on_appropriate_error_message(
            String invalidCodeErrorMessage) {
        // given
        mockThereIsNoSuchElement(NEMID_TOKEN);
        mockThereIsWebElementWithText(NOT_EMPTY_ERROR_MESSAGE, invalidCodeErrorMessage);

        // when
        Throwable throwable =
                catchThrowable(() -> getTokenStep.enterCodeAndGetToken(CODE_CARD_CODE));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.INVALID_CODE_CARD_CODE.exception());

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NOT_EMPTY_ERROR_MESSAGE);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters({"Fejl123 i nøgle", "some unknown message"})
    public void should_throw_credentials_verification_error_on_unknown_error_message(
            String unknownErrorMessage) {
        // given
        mockThereIsNoSuchElement(NEMID_TOKEN);
        mockThereIsWebElementWithText(NOT_EMPTY_ERROR_MESSAGE, unknownErrorMessage);

        // when
        Throwable throwable =
                catchThrowable(() -> getTokenStep.enterCodeAndGetToken(CODE_CARD_CODE));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NOT_EMPTY_ERROR_MESSAGE);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_try_to_find_token_or_error_message_for_10_seconds_and_then_fail() {
        // given
        mockThereIsNoSuchElement(NEMID_TOKEN);
        mockThereIsNoSuchElement(NOT_EMPTY_ERROR_MESSAGE);

        // when
        Throwable throwable =
                catchThrowable(() -> getTokenStep.enterCodeAndGetToken(CODE_CARD_CODE));

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
                    mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
                    mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
                    mocksToVerifyInOrder
                            .verify(driverWrapper)
                            .tryFindElement(NOT_EMPTY_ERROR_MESSAGE);
                    mocksToVerifyInOrder.verify(driverWrapper).sleepFor(1_000);
                },
                10);
    }

    private void mockThereIsWebElementWithText(By elementSelector, String text) {
        WebElement element = webElementMockWithText(text);
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.of(element));
    }

    private void mockThereIsNoSuchElement(By elementSelector) {
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.empty());
    }
}

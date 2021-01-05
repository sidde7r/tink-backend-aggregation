package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_TIMEOUT_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.verifyNTimes;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.webElementMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.util.NemIdTestHelper.webElementMockWithText;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;

@RequiredArgsConstructor
public class NemIdCollectTokenStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private NemIdTokenValidator tokenValidator;

    private NemIdCollectTokenStep collectTokenStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        tokenValidator = mock(NemIdTokenValidator.class);

        collectTokenStep =
                new NemIdCollectTokenStep(driverWrapper, nemIdMetricsMock(), tokenValidator);

        mocksToVerifyInOrder = inOrder(driverWrapper, tokenValidator);
    }

    @Test
    public void should_return_valid_token() {
        // given
        WebElement nemIdTokenElement = webElementMockWithText("--- SAMPLE TOKEN ---");
        when(driverWrapper.tryFindElement(NEMID_TOKEN)).thenReturn(Optional.of(nemIdTokenElement));

        mockThatTokenIsValid();

        // when
        String nemIdToken = collectTokenStep.collectToken();

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("--- SAMPLE TOKEN ---");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_token_validation_error() {
        // given
        WebElement nemIdTokenElement = webElementMockWithText("--- SAMPLE TOKEN 123 ---");
        when(driverWrapper.tryFindElement(NEMID_TOKEN)).thenReturn(Optional.of(nemIdTokenElement));

        Throwable tokenValidationError = new RuntimeException("invalid token");
        mockThatTokenIsInvalid(tokenValidationError);

        // when
        Throwable throwable = catchThrowable(() -> collectTokenStep.collectToken());

        // then
        assertThat(throwable).isEqualTo(tokenValidationError);

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("--- SAMPLE TOKEN 123 ---");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_look_for_not_empty_token_and_if_there_is_an_iframe_also_for_timeout_icon() {
        // given
        when(driverWrapper.tryFindElement(NEMID_TOKEN))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(webElementMockWithText("")))
                .thenReturn(Optional.of(webElementMockWithText("--- SAMPLE TOKEN ---")));

        when(driverWrapper.trySwitchToNemIdIframe()).thenReturn(true);
        mockThatTokenIsValid();

        // when
        String nemIdToken = collectTokenStep.collectToken();

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
                    mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
                    mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
                    mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TIMEOUT_ICON);
                },
                2);
        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("--- SAMPLE TOKEN ---");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_look_only_for_not_empty_token_if_there_is_no_iframe() {
        // given
        when(driverWrapper.tryFindElement(NEMID_TOKEN))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(webElementMockWithText("")))
                .thenReturn(Optional.of(webElementMockWithText("--- SAMPLE TOKEN ---")));

        when(driverWrapper.trySwitchToNemIdIframe()).thenReturn(false);
        mockThatTokenIsValid();

        // when
        String nemIdToken = collectTokenStep.collectToken();

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
                    mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
                },
                3);
        mocksToVerifyInOrder.verify(tokenValidator).verifyTokenIsValid("--- SAMPLE TOKEN ---");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_timeout_exception_when_timeout_icon_is_found() {
        // given
        when(driverWrapper.tryFindElement(NEMID_TOKEN)).thenReturn(Optional.empty());
        when(driverWrapper.tryFindElement(NEMID_TIMEOUT_ICON))
                .thenReturn(Optional.of(webElementMock()));

        when(driverWrapper.trySwitchToNemIdIframe()).thenReturn(true);

        // when
        Throwable throwable = catchThrowable(() -> collectTokenStep.collectToken());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, NemIdError.TIMEOUT.exception());

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
        mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TIMEOUT_ICON);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_try_to_find_token_or_timeout_for_a_little_longer_than_nem_id_timeout_and_then_fail() {
        // given
        when(driverWrapper.tryFindElement(NEMID_TOKEN)).thenReturn(Optional.empty());
        when(driverWrapper.tryFindElement(NEMID_TIMEOUT_ICON)).thenReturn(Optional.empty());

        when(driverWrapper.trySwitchToNemIdIframe()).thenReturn(true);

        // when
        Throwable throwable = catchThrowable(() -> collectTokenStep.collectToken());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, LoginError.CREDENTIALS_VERIFICATION_ERROR.exception());

        verifyNTimes(
                () -> {
                    mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
                    mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
                    mocksToVerifyInOrder.verify(driverWrapper).trySwitchToNemIdIframe();
                    mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TIMEOUT_ICON);
                    mocksToVerifyInOrder.verify(driverWrapper).sleepFor(1_000);
                },
                NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN);
    }

    private void mockThatTokenIsValid() {
        doNothing().when(tokenValidator).verifyTokenIsValid(anyString());
    }

    private void mockThatTokenIsInvalid(Throwable tokenValidationError) {
        doThrow(tokenValidationError).when(tokenValidator).verifyTokenIsValid(anyString());
    }
}

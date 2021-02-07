package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TIMEOUT_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.nemIdMetricsMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyNTimes;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper.webElementMockWithText;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

public class NemIdCodeAppCollectTokenStepTest {

    private NemIdWebDriverWrapper driverWrapper;

    private NemIdCodeAppCollectTokenStep collectTokenStep;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);

        collectTokenStep = new NemIdCodeAppCollectTokenStep(driverWrapper, nemIdMetricsMock());

        mocksToVerifyInOrder = inOrder(driverWrapper);
    }

    @Test
    public void should_return_correct_token() {
        // given
        mockThereIsWebElementWithText(NEMID_TOKEN, "--- SAMPLE TOKEN ---");

        // when
        String nemIdToken = collectTokenStep.collectToken();

        // then
        assertThat(nemIdToken).isEqualTo("--- SAMPLE TOKEN ---");

        mocksToVerifyInOrder.verify(driverWrapper).switchToParentWindow();
        mocksToVerifyInOrder.verify(driverWrapper).tryFindElement(NEMID_TOKEN);
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
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_timeout_exception_when_timeout_icon_is_found() {
        // given
        mockThereIsNoSuchElement(NEMID_TOKEN);
        mockThereIsWebElement(NEMID_TIMEOUT_ICON, webElementMock());

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
        mockThereIsNoSuchElement(NEMID_TOKEN);
        mockThereIsNoSuchElement(NEMID_TIMEOUT_ICON);

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

    @SuppressWarnings("SameParameterValue")
    private void mockThereIsWebElement(By elementSelector, WebElement element) {
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.of(element));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockThereIsWebElementWithText(By elementSelector, String text) {
        WebElement element = webElementMockWithText(text);
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.of(element));
    }

    private void mockThereIsNoSuchElement(By elementSelector) {
        when(driverWrapper.tryFindElement(elementSelector)).thenReturn(Optional.empty());
    }
}

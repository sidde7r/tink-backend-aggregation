package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CLOSE_SELECT_METHOD_POPUP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdTestHelper;

@RunWith(JUnitParamsRunner.class)
public class NemIdSwitchTo2FAScreenStepTest {

    private NemIdWebDriverWrapper driverWrapper;
    private InOrder mocksToVerifyInOrder;

    private NemIdSwitchTo2FAScreenStep switchTo2FAScreenStep;

    @Before
    public void setup() {
        driverWrapper = mock(NemIdWebDriverWrapper.class);
        mocksToVerifyInOrder = inOrder(driverWrapper);

        switchTo2FAScreenStep = new NemIdSwitchTo2FAScreenStep(driverWrapper);
    }

    @Test
    @Parameters(method = "canOnlyUseDefaultMethodTestParams")
    public void should_not_switch_screen_when_the_detection_result_was_can_only_use_default_method(
            NemIdDetect2FAMethodsResult detect2FAMethodsResult,
            NemId2FAMethodScreen screenForMethodChosenByUser) {
        // when
        switchTo2FAScreenStep.switchTo2FAMethodScreen(
                detect2FAMethodsResult, screenForMethodChosenByUser);

        // then
        verifyWaits10SecondsForScreenToLoad(screenForMethodChosenByUser);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] canOnlyUseDefaultMethodTestParams() {
        return Stream.of(NemId2FAMethodScreen.values())
                .map(
                        defaultScreen ->
                                SwitchScreenTestParams.builder()
                                        .detect2FAMethodsResult(
                                                NemIdDetect2FAMethodsResult.canOnlyUseDefaultMethod(
                                                        defaultScreen))
                                        .screenChosenByUser(defaultScreen)
                                        .build())
                .map(SwitchScreenTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    @Test
    @Parameters(method = "canToggleBetweenMethodsButIsAlreadyOnCorrectOneTestParams")
    public void
            should_stay_on_toggled_screen_if_user_chose_it_and_the_detection_result_was_can_toggle_between_methods(
                    NemIdDetect2FAMethodsResult detect2FAMethodsResult,
                    NemId2FAMethodScreen screenForMethodChosenByUser) {
        // when
        switchTo2FAScreenStep.switchTo2FAMethodScreen(
                detect2FAMethodsResult, screenForMethodChosenByUser);

        // then
        verifyWaits10SecondsForScreenToLoad(screenForMethodChosenByUser);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] canToggleBetweenMethodsButIsAlreadyOnCorrectOneTestParams() {
        return NemIdTestHelper.allNemId2FAPairwiseDifferentScreens().stream()
                .map(
                        pairwiseDifferentScreens -> {
                            NemId2FAMethodScreen defaultScreen = pairwiseDifferentScreens._1;
                            NemId2FAMethodScreen screenWeToggledTo = pairwiseDifferentScreens._2;

                            return SwitchScreenTestParams.builder()
                                    .detect2FAMethodsResult(
                                            NemIdDetect2FAMethodsResult.canToggleBetween2Methods(
                                                    defaultScreen, screenWeToggledTo))
                                    .screenChosenByUser(screenWeToggledTo)
                                    .build();
                        })
                .map(SwitchScreenTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    @Test
    @Parameters(method = "canToggleBetweenMethodsButIsNotCurrentlyOnCorrectOneTestParams")
    public void
            should_switch_back_to_default_screen_if_user_chose_it_and_the_detection_result_was_can_toggle_between_methods(
                    NemIdDetect2FAMethodsResult detect2FAMethodsResult,
                    NemId2FAMethodScreen screenForMethodChosenByUser) {
        // when
        switchTo2FAScreenStep.switchTo2FAMethodScreen(
                detect2FAMethodsResult, screenForMethodChosenByUser);

        // then
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .clickButton(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
        verifyWaits10SecondsForScreenToLoad(screenForMethodChosenByUser);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] canToggleBetweenMethodsButIsNotCurrentlyOnCorrectOneTestParams() {
        return NemIdTestHelper.allNemId2FAPairwiseDifferentScreens().stream()
                .map(
                        pairwiseDifferentScreens -> {
                            NemId2FAMethodScreen defaultScreen = pairwiseDifferentScreens._1;
                            NemId2FAMethodScreen screenWeToggledTo = pairwiseDifferentScreens._2;

                            return SwitchScreenTestParams.builder()
                                    .detect2FAMethodsResult(
                                            NemIdDetect2FAMethodsResult.canToggleBetween2Methods(
                                                    defaultScreen, screenWeToggledTo))
                                    .screenChosenByUser(defaultScreen)
                                    .build();
                        })
                .map(SwitchScreenTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    @Test
    @Parameters(method = "canChooseFromPopupAndIsAlreadyOnCorrectScreenTestParams")
    public void
            should_close_popup_and_stay_on_already_correct_screen_when_the_detection_result_was_can_choose_method_from_popup(
                    NemIdDetect2FAMethodsResult detect2FAMethodsResult,
                    NemId2FAMethodScreen screenForMethodChosenByUser) {
        // when
        switchTo2FAScreenStep.switchTo2FAMethodScreen(
                detect2FAMethodsResult, screenForMethodChosenByUser);

        // then
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(NEMID_CLOSE_SELECT_METHOD_POPUP);
        verifyWaits10SecondsForScreenToLoad(screenForMethodChosenByUser);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] canChooseFromPopupAndIsAlreadyOnCorrectScreenTestParams() {
        return Stream.of(NemId2FAMethodScreen.values())
                .map(
                        defaultScreen ->
                                SwitchScreenTestParams.builder()
                                        .detect2FAMethodsResult(
                                                NemIdDetect2FAMethodsResult
                                                        .canChooseMethodFromPopup(
                                                                defaultScreen,
                                                                getAllMethodsAsSet()))
                                        .screenChosenByUser(defaultScreen)
                                        .build())
                .map(SwitchScreenTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    @Test
    @Parameters(method = "canChooseFromPopupAndIsNotAlreadyOnCorrectScreenTestParams")
    public void
            should_click_button_to_switch_screen_when_the_detection_result_was_can_choose_method_from_popup(
                    NemIdDetect2FAMethodsResult detect2FAMethodsResult,
                    NemId2FAMethodScreen screenForMethodChosenByUser) {
        // given
        By buttonExpectedToBeClicked =
                NemIdSelect2FAPopupOptionButton.getOptionButtonThatWillSwitchToScreen(
                                screenForMethodChosenByUser)
                        .getButtonSelector();

        // when
        switchTo2FAScreenStep.switchTo2FAMethodScreen(
                detect2FAMethodsResult, screenForMethodChosenByUser);

        // then
        mocksToVerifyInOrder.verify(driverWrapper).clickButton(buttonExpectedToBeClicked);
        verifyWaits10SecondsForScreenToLoad(screenForMethodChosenByUser);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private Object[] canChooseFromPopupAndIsNotAlreadyOnCorrectScreenTestParams() {
        return NemIdTestHelper.allNemId2FAPairwiseDifferentScreens().stream()
                .map(
                        pairwiseDifferentScreens -> {
                            NemId2FAMethodScreen defaultScreen = pairwiseDifferentScreens._1;
                            NemId2FAMethodScreen screenChosenByUser = pairwiseDifferentScreens._2;

                            return SwitchScreenTestParams.builder()
                                    .detect2FAMethodsResult(
                                            NemIdDetect2FAMethodsResult.canChooseMethodFromPopup(
                                                    defaultScreen, getAllMethodsAsSet()))
                                    .screenChosenByUser(screenChosenByUser)
                                    .build();
                        })
                .map(SwitchScreenTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    private void verifyWaits10SecondsForScreenToLoad(NemId2FAMethodScreen screen) {
        mocksToVerifyInOrder
                .verify(driverWrapper)
                .waitForElement(screen.getSelectorToDetectScreen(), 10);
    }

    private Set<NemId2FAMethod> getAllMethodsAsSet() {
        return Stream.of(NemId2FAMethod.values()).collect(Collectors.toSet());
    }

    @Data
    @Builder
    private static class SwitchScreenTestParams {
        private final NemIdDetect2FAMethodsResult detect2FAMethodsResult;
        private final NemId2FAMethodScreen screenChosenByUser;

        private Object[] toMethodParams() {
            return new Object[] {detect2FAMethodsResult, screenChosenByUser};
        }
    }
}

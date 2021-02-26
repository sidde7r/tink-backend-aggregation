package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CLOSE_SELECT_METHOD_POPUP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod.NemIdDetect2FAMethodsResult.ResultType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
class NemIdSwitchTo2FAScreenStep {

    private final NemIdWebDriverWrapper driverWrapper;

    public void switchTo2FAMethodScreen(
            NemIdDetect2FAMethodsResult detectMethodsResult, NemId2FAMethodScreen targetScreen) {

        switchToTarget2FAScreen(detectMethodsResult, targetScreen);
        driverWrapper.waitForElement(targetScreen.getSelectorToDetectScreen(), 10);
    }

    private void switchToTarget2FAScreen(
            NemIdDetect2FAMethodsResult detect2FAMethodsResult, NemId2FAMethodScreen targetScreen) {

        NemId2FAMethodScreen currentScreen = detect2FAMethodsResult.getCurrentScreen();
        ResultType resultType = detect2FAMethodsResult.getResultType();

        switch (resultType) {
            case CAN_ONLY_USE_DEFAULT_METHOD:
                break;

            case CAN_TOGGLE_BETWEEN_2_METHODS:
                handleCanToggleBetweenMethods(currentScreen, targetScreen);
                break;

            case CAN_CHOOSE_METHOD_FROM_POPUP:
                handleCanChooseMethodFromPopup(currentScreen, targetScreen);
                break;

            default:
                throw new IllegalStateException(
                        "Unknown detect 2FA methods result type: " + resultType);
        }
    }

    private void handleCanToggleBetweenMethods(
            NemId2FAMethodScreen currentScreen, NemId2FAMethodScreen targetScreen) {
        if (targetScreen != currentScreen) {
            toggleBetween2FAMethodScreens();
        }
    }

    private void handleCanChooseMethodFromPopup(
            NemId2FAMethodScreen currentScreen, NemId2FAMethodScreen targetScreen) {
        if (targetScreen == currentScreen) {
            closePopup();
            return;
        }
        NemIdSelect2FAPopupOptionButton optionButton =
                NemIdSelect2FAPopupOptionButton.getOptionButtonThatWillSwitchToScreen(targetScreen);
        clickOptionButton(optionButton);
    }

    private void closePopup() {
        driverWrapper.clickButton(NEMID_CLOSE_SELECT_METHOD_POPUP);
    }

    private void toggleBetween2FAMethodScreens() {
        driverWrapper.clickButton(NEMID_LINK_TO_SELECT_DIFFERENT_2FA_METHOD);
    }

    private void clickOptionButton(NemIdSelect2FAPopupOptionButton optionButton) {
        driverWrapper.clickButton(optionButton.getButtonSelector());
    }
}

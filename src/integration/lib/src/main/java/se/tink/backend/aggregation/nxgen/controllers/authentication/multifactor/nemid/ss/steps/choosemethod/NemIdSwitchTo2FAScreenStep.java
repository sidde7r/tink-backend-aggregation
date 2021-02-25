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
public class NemIdSwitchTo2FAScreenStep {

    private final NemIdWebDriverWrapper driverWrapper;

    public void switchTo2FAMethodScreen(
            NemIdDetect2FAMethodsResult detectMethodsResult, NemId2FAMethodScreen targetScreen) {

        switchToTarget2FAScreen(detectMethodsResult, targetScreen);
        driverWrapper.waitForElement(targetScreen.getSelectorToDetectScreen(), 10);
    }

    private void switchToTarget2FAScreen(
            NemIdDetect2FAMethodsResult detect2FAMethodsResult, NemId2FAMethodScreen targetScreen) {

        NemId2FAMethodScreen defaultScreen = detect2FAMethodsResult.getDefaultScreen();
        NemId2FAMethodScreen currentScreen = detect2FAMethodsResult.getCurrentScreen();

        ResultType resultType = detect2FAMethodsResult.getResultType();

        if (resultType == ResultType.CAN_ONLY_USE_DEFAULT_METHOD) {
            return;
        }

        if (resultType == ResultType.CAN_TOGGLE_BETWEEN_2_METHODS) {
            if (targetScreen != currentScreen) {
                toggleBetween2FAMethodScreens();
            }
            return;
        }

        if (resultType == ResultType.CAN_CHOOSE_METHOD_FROM_POPUP) {
            if (targetScreen == defaultScreen) {
                closePopup();
            } else {
                NemIdSelect2FAPopupOptionButton optionButton =
                        NemIdSelect2FAPopupOptionButton.getOptionButtonThatWillSwitchToScreen(
                                targetScreen);
                clickOptionButton(optionButton);
            }
            return;
        }

        throw new IllegalStateException("Unknown detect 2FA methods result type: " + resultType);
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

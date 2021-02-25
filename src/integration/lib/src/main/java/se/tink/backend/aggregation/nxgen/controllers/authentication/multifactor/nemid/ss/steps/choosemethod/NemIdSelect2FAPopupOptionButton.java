package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;

@Getter
@RequiredArgsConstructor
public enum NemIdSelect2FAPopupOptionButton {
    CODE_APP_BUTTON(
            NemId2FAMethodScreen.CODE_APP_SCREEN, HtmlElements.NEMID_SELECT_CODE_APP_BUTTON),
    CODE_CARD_BUTTON(
            NemId2FAMethodScreen.CODE_CARD_SCREEN, HtmlElements.NEMID_SELECT_CODE_CARD_BUTTON),
    CODE_TOKEN_BUTTON(
            NemId2FAMethodScreen.CODE_TOKEN_SCREEN, HtmlElements.NEMID_SELECT_CODE_TOKEN_BUTTON);

    private final NemId2FAMethodScreen screenThatButtonSwitchesTo;
    private final By buttonSelector;

    public static List<By> getSelectorsForAllButtons() {
        return Stream.of(NemIdSelect2FAPopupOptionButton.values())
                .map(NemIdSelect2FAPopupOptionButton::getButtonSelector)
                .collect(Collectors.toList());
    }

    public static Optional<NemIdSelect2FAPopupOptionButton> getBySelector(By selector) {
        return Stream.of(NemIdSelect2FAPopupOptionButton.values())
                .filter(option -> option.getButtonSelector().equals(selector))
                .findFirst();
    }

    public static NemIdSelect2FAPopupOptionButton getOptionButtonThatWillSwitchToScreen(
            NemId2FAMethodScreen screen) {
        return Stream.of(NemIdSelect2FAPopupOptionButton.values())
                .filter(optionButton -> optionButton.getScreenThatButtonSwitchesTo().equals(screen))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "There is no option button that allows to pick screen: "
                                                + screen));
    }
}

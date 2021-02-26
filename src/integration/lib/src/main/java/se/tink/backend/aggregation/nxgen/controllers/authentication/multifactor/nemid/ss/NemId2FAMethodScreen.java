package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements;

/**
 * This class represents a NemID iframe page/screen dedicated to run a specific {@link
 * NemId2FAMethod}.
 */
@Getter
@RequiredArgsConstructor
public enum NemId2FAMethodScreen {
    CODE_APP_SCREEN(HtmlElements.NEMID_CODE_APP_SCREEN, NemId2FAMethod.CODE_APP),
    CODE_CARD_SCREEN(HtmlElements.NEMID_CODE_CARD_SCREEN, NemId2FAMethod.CODE_CARD),
    CODE_TOKEN_SCREEN(HtmlElements.NEMID_CODE_TOKEN_SCREEN, NemId2FAMethod.CODE_TOKEN);

    // Selector that allows to detect that we're on a given screen
    private final By selectorToDetectScreen;

    // Which 2FA method can be used on a given screen
    private final NemId2FAMethod supportedMethod;

    public static List<By> getSelectorsForAllScreens() {
        return Stream.of(NemId2FAMethodScreen.values())
                .map(NemId2FAMethodScreen::getSelectorToDetectScreen)
                .collect(Collectors.toList());
    }

    public static Optional<NemId2FAMethodScreen> getScreenBySelector(By selector) {
        return Stream.of(NemId2FAMethodScreen.values())
                .filter(screen -> screen.getSelectorToDetectScreen() == selector)
                .findFirst();
    }

    public static NemId2FAMethodScreen getScreenBy2FAMethod(NemId2FAMethod method) {
        return Stream.of(NemId2FAMethodScreen.values())
                .filter(screen -> screen.getSupportedMethod() == method)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot find screen for method: " + method));
    }
}

package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import static java.util.stream.Collectors.toList;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementLocator;

@Getter
@RequiredArgsConstructor
public enum BankIdScreen {
    ENTER_SSN_SCREEN(HtmlLocators.LOC_ENTER_SSN_SCREEN),

    ONE_TIME_CODE_METHOD_SCREEN(HtmlLocators.LOC_ONE_TIME_CODE_METHOD_SCREEN),
    MOBILE_BANK_ID_SEND_REQUEST_SCREEN(HtmlLocators.LOC_MOBILE_BANK_ID_SEND_REQUEST_SCREEN),
    MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN(
            HtmlLocators.LOC_MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN),
    MOBILE_BANK_ID_REFERENCE_WORDS_SCREEN(HtmlLocators.LOC_MOBILE_BANK_ID_REFERENCE_WORDS_SCREEN),
    THIRD_PARTY_APP_METHOD_SCREEN(HtmlLocators.LOC_THIRD_PARTY_APP_METHOD_SCREEN),
    CHOOSE_2FA_METHOD_SCREEN(HtmlLocators.LOC_CHOOSE_2FA_METHOD_SCREEN),

    BANK_ID_ERROR_WITH_HEADING_SCREEN(HtmlLocators.LOC_BANK_ID_ERROR_WITH_HEADING_SCREEN),
    BANK_ID_ERROR_NO_HEADING_SCREEN(HtmlLocators.LOC_BANK_ID_ERROR_NO_HEADING_SCREEN),
    ENTER_BANK_ID_PASSWORD_SCREEN(HtmlLocators.LOC_PRIVATE_PASSWORD_SCREEN);

    private static final List<BankIdScreen> ALL_2FA_METHOD_SCREENS =
            ImmutableList.of(
                    ONE_TIME_CODE_METHOD_SCREEN,
                    MOBILE_BANK_ID_SEND_REQUEST_SCREEN,
                    MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN,
                    THIRD_PARTY_APP_METHOD_SCREEN);

    public static final Map<BankIdScreen, ElementLocator>
            ALL_ERROR_SCREENS_WITH_ERROR_TEXT_LOCATORS =
                    ImmutableMap.<BankIdScreen, ElementLocator>builder()
                            .put(
                                    BANK_ID_ERROR_WITH_HEADING_SCREEN,
                                    HtmlLocators.LOC_BANK_ID_ERROR_WITH_HEADING_TEXT)
                            .put(
                                    BANK_ID_ERROR_NO_HEADING_SCREEN,
                                    HtmlLocators.LOC_BANK_ID_ERROR_NO_HEADING_TEXT)
                            .build();

    public static List<BankIdScreen> getAllScreens() {
        return Stream.of(BankIdScreen.values()).collect(toList());
    }

    public static List<BankIdScreen> getAll2FAMethodScreens() {
        return ALL_2FA_METHOD_SCREENS;
    }

    public static List<BankIdScreen> getAllErrorScreens() {
        return new ArrayList<>(ALL_ERROR_SCREENS_WITH_ERROR_TEXT_LOCATORS.keySet());
    }

    public static List<BankIdScreen> getAllNonErrorScreens() {
        List<BankIdScreen> allScreens = getAllScreens();
        List<BankIdScreen> errorScreens = getAllErrorScreens();
        return allScreens.stream()
                .filter(screen -> !errorScreens.contains(screen))
                .collect(toList());
    }

    public static BankIdScreen findScreenByItsLocator(ElementLocator locator) {
        return tryFindScreenByItsLocator(locator)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "%s Cannot find any screen for locator: %s",
                                                BANK_ID_LOG_PREFIX, locator)));
    }

    public static Optional<BankIdScreen> tryFindScreenByItsLocator(ElementLocator locator) {
        return getAllScreens().stream()
                .filter(screen -> screen.getLocatorToDetectScreen() == locator)
                .findFirst();
    }

    // locator that allows to detect that we're on a given screen
    private final ElementLocator locatorToDetectScreen;

    public boolean isErrorScreen() {
        return getAllErrorScreens().contains(this);
    }
}

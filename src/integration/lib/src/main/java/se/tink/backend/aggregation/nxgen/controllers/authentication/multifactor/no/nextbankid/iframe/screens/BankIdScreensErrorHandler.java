package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOErrorCode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class BankIdScreensErrorHandler {

    private final BankIdWebDriver webDriver;

    /**
     * Throw exception when we found screen that we were not looking for (or didn't find any screen
     * at all). Additionally, if it's an error screen, try to extract some error codes to figure out
     * what's the reason for it.
     */
    public void throwUnexpectedScreenException(
            @Nullable BankIdScreen unexpectedScreenFound, List<BankIdScreen> expectedScreens) {

        if (unexpectedScreenFound == null) {
            throw new IllegalStateException(
                    String.format(
                            "%s Could not find any known screen. (expectedScreens: %s)",
                            BANK_ID_LOG_PREFIX, expectedScreens));
        }

        if (!unexpectedScreenFound.isErrorScreen()) {
            throw new IllegalStateException(
                    String.format(
                            "%s Unexpected non error screen: %s (expectedScreens: %s)",
                            BANK_ID_LOG_PREFIX, unexpectedScreenFound, expectedScreens));
        }

        throwUnexpectedErrorScreenException(unexpectedScreenFound, expectedScreens);
    }

    private void throwUnexpectedErrorScreenException(
            BankIdScreen errorScreen, List<BankIdScreen> expectedScreens) {
        BankIdElementLocator screenTextLocator =
                BankIdScreen.ALL_ERROR_SCREENS_WITH_ERROR_TEXT_LOCATORS.get(errorScreen);

        String screenText =
                getElementText(screenTextLocator)
                        .orElseThrow(
                                () ->
                                        BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception(
                                                String.format(
                                                        "%s Cannot read text from BankID error screen: %s (expectedScreens: %s)",
                                                        BANK_ID_LOG_PREFIX,
                                                        errorScreen,
                                                        expectedScreens)));

        BankIdNOErrorCode errorCode =
                extractErrorCode(screenText)
                        .orElseThrow(
                                () ->
                                        BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception(
                                                String.format(
                                                        "%s Cannot match BankID error code by error text on screen: %s (expectedScreens: %s)\n."
                                                                + "Error screen text: %s",
                                                        BANK_ID_LOG_PREFIX,
                                                        errorScreen,
                                                        expectedScreens,
                                                        screenText)));

        throw errorCode
                .getError()
                .exception(
                        String.format(
                                "%s BankID error: (%s). (error screen: %s) (expectedScreens: %s)",
                                BANK_ID_LOG_PREFIX, errorCode, errorScreen, expectedScreens));
    }

    private Optional<String> getElementText(BankIdElementLocator elementSelector) {
        return webDriver
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(elementSelector)
                                .waitForSeconds(10)
                                .build())
                .getFirstFoundElement()
                .map(WebElement::getText)
                .map(String::trim);
    }

    private Optional<BankIdNOErrorCode> extractErrorCode(String errorMessage) {
        return Stream.of(BankIdNOErrorCode.values())
                .filter(
                        errorCode ->
                                errorMessage
                                        .toLowerCase()
                                        .contains(errorCode.getCode().toLowerCase()))
                .findFirst();
    }
}

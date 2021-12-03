package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.HtmlLocators;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants.Messages.INVALID_SSN_ERROR_MESSAGE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeInitializer;
import se.tink.backend.aggregation.utils.ExecutionTimer;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchResult;

@Slf4j
@RequiredArgsConstructor
public class DnbBankIdIframeInitializer implements BankIdIframeInitializer {

    private final Credentials credentials;

    @Override
    public BankIdIframeFirstWindow initializeIframe(WebDriverService webDriver) {
        openDnbWebsite(webDriver);
        waitForSSNInput(webDriver);
        tryClosingCookiesWindow(webDriver);

        enterSSN(webDriver);
        clickSubmitButton(webDriver);

        verifyNoErrors(webDriver);

        return BankIdIframeFirstWindow.AUTHENTICATE_WITH_DEFAULT_2FA_METHOD;
    }

    private void openDnbWebsite(WebDriverService webDriver) {
        webDriver.get(DnbConstants.Url.INIT_LOGIN);
    }

    private void waitForSSNInput(WebDriverService webDriver) {
        webDriver
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder().searchFor(HtmlLocators.LOC_SSN_INPUT).build())
                .getFirstFoundElement()
                .orElseThrow(() -> new IllegalStateException("[DNB] SSN input field not found"));
    }

    private void tryClosingCookiesWindow(WebDriverService webDriver) {
        /*
        Sometimes popup with cookies can appear after many seconds - that's why we should wait for quite a long time
        here. We should monitor how often it happens and if it's frequent we might need to make changes in BankID
        controller so it will be ready for cookies window at any time.
         */
        ExecutionTimer timer = new ExecutionTimer();
        Optional<WebElement> maybeCloseCookiesButton =
                timer.execute(
                        () ->
                                webDriver
                                        .searchForFirstMatchingLocator(
                                                ElementsSearchQuery.builder()
                                                        .searchFor(
                                                                HtmlLocators
                                                                        .LOC_CLOSE_COOKIES_POPUP_BUTTON)
                                                        .searchForSeconds(10)
                                                        .build())
                                        .getFirstFoundElement());
        log.info(
                "[DNB] Waiting for cookies window took: [{}]s. Is window found: [{}].",
                timer.getDurationInSeconds(),
                maybeCloseCookiesButton.isPresent());

        maybeCloseCookiesButton.ifPresent(WebElement::click);
    }

    private void enterSSN(WebDriverService webDriver) {
        log.info("[DNB] Setting SSN input value");
        webDriver.setValueToElement(
                credentials.getField(Field.Key.USERNAME), HtmlLocators.LOC_SSN_INPUT);
    }

    private void clickSubmitButton(WebDriverService webDriver) {
        log.info("[DNB] Clicking submit button");
        webDriver.clickButton(HtmlLocators.LOC_SUBMIT_BUTTON);
    }

    private void verifyNoErrors(WebDriverService webDriver) {
        ElementsSearchResult searchResult =
                webDriver.searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(
                                        BankIdConstants.HtmlLocators.LOC_IFRAME,
                                        HtmlLocators.LOC_ERROR_MESSAGE)
                                .build());
        if (searchResult.isEmpty()) {
            throw BankIdNOError.INITIALIZATION_ERROR.exception();
        }

        ElementLocator locatorFound = searchResult.getLocatorFound();
        if (locatorFound == BankIdConstants.HtmlLocators.LOC_IFRAME) {
            log.info("[DNB] BankID iframe initialized successfully");
            return;
        }
        if (locatorFound == HtmlLocators.LOC_ERROR_MESSAGE) {
            throwErrorMessage(searchResult);
        }
    }

    private void throwErrorMessage(ElementsSearchResult searchResult) {
        String errorMessage =
                searchResult
                        .getFirstFoundElement()
                        .map(element -> element.getAttribute("textContent"))
                        .orElse(null);

        if (containsIgnoreCase(errorMessage, INVALID_SSN_ERROR_MESSAGE)) {
            throw BankIdNOError.INVALID_SSN.exception();
        }
        throw BankIdNOError.UNKNOWN_BANK_ID_ERROR.exception(
                "[DNB] Unknown BankID iframe initialization error: " + errorMessage);
    }
}

package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants.Xpath;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.IframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.exceptions.HtmlElementNotFoundException;
import se.tink.integration.webdriver.exceptions.ScreenScrapingException;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
@Slf4j
public class BankIdIframeSSAuthenticationController {

    private static final int WAIT_RENDER_MILLIS = 2000;

    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final IframeInitializer iframeInitializer;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    public void doLogin(String password) throws AuthenticationException {
        iframeInitializer.initializeBankIdAuthentication();

        if (!isMobileBankIdInputPresent()) {
            if (isBankIdApp()) {
                // if it is BankID-app we should just wait for user action
                waitForUserInteractionAndSendBankIdPassword(driver, password);
                return;
            }
            getListAuthenticationMethods(driver);
            chooseBankIdMobil(driver);
        }
        webDriverHelper.sleep(WAIT_RENDER_MILLIS);
        webDriverHelper.submitForm(driver, Xpath.FORM_XPATH);
        displayReferenceWords();
        waitForUserInteractionAndSendBankIdPassword(driver, password);
    }

    private boolean isMobileBankIdInputPresent() {
        // looks for unique element for mobile bank id
        return webDriverHelper.waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH).isPresent();
    }

    private boolean isBankIdApp() {
        return webDriverHelper
                .waitForOneOfElements(
                        driver, Xpath.BANK_ID_APP_TITLE_XPATH, Xpath.BANK_ID_PASSWORD_INPUT_XPATH)
                .isPresent();
    }

    private void getListAuthenticationMethods(WebDriver driver) {
        WebElement selectAuthenticationButton =
                webDriverHelper
                        .waitForElement(driver, Xpath.AUTHENTICATION_LIST_BUTTON_XPATH)
                        .orElseThrow(LoginError.NO_AVAILABLE_SCA_METHODS::exception);
        webDriverHelper.clickButton(selectAuthenticationButton);
    }

    private void chooseBankIdMobil(WebDriver driver) {
        try {
            WebElement bankIdMobilAuthenticationSelectionButton =
                    webDriverHelper.getElement(driver, Xpath.BANK_ID_MOBIL_BUTTON);
            webDriverHelper.clickButton(bankIdMobilAuthenticationSelectionButton);
        } catch (HtmlElementNotFoundException e) {
            log.warn(
                    "There is no mobile bank id, please check source to find methods: {}",
                    driver.getPageSource());
            throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    private String getReferenceWords() {
        return webDriverHelper
                .waitForElement(driver, Xpath.REFERENCE_WORDS_XPATH)
                .map(WebElement::getText)
                .orElseThrow(() -> new ScreenScrapingException("Couldn't find reference words"));
    }

    private void displayReferenceWords() {
        Field field = NorwegianFields.BankIdReferenceInfo.build(catalog, getReferenceWords());

        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void waitForUserInteractionAndSendBankIdPassword(WebDriver driver, String password)
            throws LoginException {
        WebElement bankIdPasswordInputElement = waitForUserInteraction(driver);
        webDriverHelper.sendInputValue(bankIdPasswordInputElement, password);
        webDriverHelper.submitForm(driver, Xpath.FORM_XPATH);
    }

    private WebElement waitForUserInteraction(WebDriver driver) throws LoginException {
        for (int i = 0; i < 90; i++, webDriverHelper.sleep(WAIT_RENDER_MILLIS)) {
            webDriverHelper.switchToIframe(driver);
            Optional<WebElement> webElement =
                    driver.findElements(Xpath.PASSWORD_INPUT_XPATH).stream()
                            .findAny()
                            .filter(webDriverHelper::checkIfElementEnabledIfNotWait);
            if (webElement.isPresent()) {
                return webElement.get();
            }
        }
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                "Password can't be reached, probably user did not accept bank id");
    }
}

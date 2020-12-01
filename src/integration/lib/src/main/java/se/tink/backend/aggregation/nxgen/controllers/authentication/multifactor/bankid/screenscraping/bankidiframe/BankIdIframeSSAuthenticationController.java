package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe;

import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.IframeInitializer;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@Slf4j
public class BankIdIframeSSAuthenticationController {

    private static final LocalizableKey ONLY_MOBILE_BANK_ID_MESSAGE =
            new LocalizableKey("Currently only Mobile BankID login method is supported");
    private static final int WAIT_RENDER_MILLIS = 2000;

    private static final By FORM_XPATH = By.xpath("//form");
    private static final By PASSWORD_INPUT_XPATH =
            By.xpath("//form//input[@type='password'][@maxlength]");
    private static final By AUTHENTICATION_LIST_BUTTON_XPATH =
            By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");
    private static final By BANK_ID_MOBIL_BUTTON =
            By.xpath(
                    "//ul/child::li/child::button[span[contains(text(),'mobil') and contains(text(),'BankID')]]");
    private static final By AUTHENTICATION_INPUT_XPATH =
            By.xpath("//form//input[@type='password'][@maxlength]");
    private static final By REFERENCE_WORDS_XPATH =
            By.xpath("//span[@data-bind='text: reference']");

    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final IframeInitializer iframeInitializer;
    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;

    public void doLogin(String password) throws AuthenticationException {
        iframeInitializer.initializeBankIdAuthentication();

        if (isBankIdMobilNotSetByDefault()) {
            getListAuthenticationMethods(driver);
            chooseBankIdMobil(driver);
        }
        webDriverHelper.sleep(WAIT_RENDER_MILLIS);
        webDriverHelper.submitForm(driver, FORM_XPATH);
        displayReferenceWords(credentials);
        waitForUserInteractionAndSendBankIdPassword(driver, password);
    }

    private void displayReferenceWords(Credentials credentials) {
        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(
                        Collections.singletonList(
                                NorwegianFields.BankIdInfo.build(catalog, getReferenceWords()))));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, true);
    }

    private String getReferenceWords() {
        return webDriverHelper
                .waitForElement(driver, REFERENCE_WORDS_XPATH)
                .map(WebElement::getText)
                .orElseThrow(() -> new ScreenScrapingException("Couldn't find reference words"));
    }

    private boolean isBankIdMobilNotSetByDefault() {
        Optional<WebElement> passwordInputElement =
                webDriverHelper.waitForElement(driver, AUTHENTICATION_INPUT_XPATH);

        return !passwordInputElement.isPresent() || passwordInputElement.get().isEnabled();
    }

    private void waitForUserInteractionAndSendBankIdPassword(WebDriver driver, String password)
            throws LoginException {
        WebElement bankIdPasswordInputElement = waitForUserInteraction(driver);
        webDriverHelper.sendInputValue(bankIdPasswordInputElement, password);
        webDriverHelper.submitForm(driver, FORM_XPATH);
    }

    private void getListAuthenticationMethods(WebDriver driver) {
        WebElement selectAuthenticationButton =
                webDriverHelper
                        .waitForElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH)
                        .orElseThrow(LoginError.NO_AVAILABLE_SCA_METHODS::exception);
        webDriverHelper.clickButton(selectAuthenticationButton);
    }

    private void chooseBankIdMobil(WebDriver driver) {
        try {
            WebElement bankIdMobilAuthenticationSelectionButton =
                    webDriverHelper.getElement(driver, BANK_ID_MOBIL_BUTTON);
            webDriverHelper.clickButton(bankIdMobilAuthenticationSelectionButton);
        } catch (HtmlElementNotFoundException e) {
            log.warn(
                    "There is no mobile bank id, please check source to find methods: {}",
                    driver.getPageSource());
            throw LoginError.NOT_SUPPORTED.exception(ONLY_MOBILE_BANK_ID_MESSAGE);
        }
    }

    private WebElement waitForUserInteraction(WebDriver driver) throws LoginException {
        for (int i = 0; i < 90; i++, webDriverHelper.sleep(WAIT_RENDER_MILLIS)) {
            webDriverHelper.switchToIframe(driver);
            Optional<WebElement> webElement =
                    driver.findElements(PASSWORD_INPUT_XPATH).stream()
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

package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.IframeInitializer;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;

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

    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final IframeInitializer iframeInitializer;

    public void doLogin(String password) throws AuthenticationException {
        iframeInitializer.initializeBankIdAuthentication();

        if (isBankIdMobilNotSetByDefault()) {
            getListAuthenticationMethods(driver);
            chooseBankIdMobil(driver);
        }
        webDriverHelper.sleep(WAIT_RENDER_MILLIS);
        webDriverHelper.submitForm(driver, FORM_XPATH);
        waitForUserInteractionAndSendBankIdPassword(driver, password);
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
                webDriverHelper.getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
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

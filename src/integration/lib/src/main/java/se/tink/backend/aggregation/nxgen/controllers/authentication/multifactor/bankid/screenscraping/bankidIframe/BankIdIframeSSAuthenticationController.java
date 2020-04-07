package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidIframe;

import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidIframe.initializer.IframeInitializer;
import se.tink.libraries.selenium.WebDriverHelper;

public class BankIdIframeSSAuthenticationController {
    private WebDriverHelper webDriverHelper;
    private WebDriver driver;
    private IframeInitializer iframeInitializer;

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

    private static final int WAIT_RENDER_MILLIS = 1000;

    public BankIdIframeSSAuthenticationController(
            IframeInitializer iframeInitializer, WebDriver driver, WebDriverHelper driverHelper) {
        this.webDriverHelper = driverHelper;
        this.driver = driver;
        this.iframeInitializer = iframeInitializer;
    }

    public void doLogin(String password) throws AuthenticationException {
        iframeInitializer.initializeBankIdAuthentication();

        if (isBankIdMobilNotSetByDefault()) {
            getListAuthenticationMethods(driver);
            chooseBankIdMobil(driver);
        }
        webDriverHelper.submitForm(driver, FORM_XPATH);

        waitForUserInteractionAndSendBankIdPassword(driver, password);
    }

    private boolean isBankIdMobilNotSetByDefault() throws LoginException {
        WebElement passwordInputElement =
                webDriverHelper
                        .waitForElement(driver, AUTHENTICATION_INPUT_XPATH)
                        .orElseThrow(() -> LoginError.NOT_SUPPORTED.exception());

        return passwordInputElement.isEnabled();
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
        WebElement bankIdMobilAuthenticationSelectionButton =
                webDriverHelper.getElement(driver, BANK_ID_MOBIL_BUTTON);
        webDriverHelper.clickButton(bankIdMobilAuthenticationSelectionButton);
    }

    private WebElement waitForUserInteraction(WebDriver driver) throws LoginException {
        for (int i = 0; i < 90; i++, webDriverHelper.sleep(WAIT_RENDER_MILLIS * i)) {
            webDriverHelper.switchToIframe(driver);
            Optional<WebElement> webElement =
                    driver.findElements(PASSWORD_INPUT_XPATH).stream()
                            .findAny()
                            .filter(input -> webDriverHelper.checkIfElementEnabledIfNotWait(input));
            if (webElement.isPresent()) {
                return webElement.get();
            }
        }
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                "Password can't be reached, probably user did not accept bank id");
    }
}

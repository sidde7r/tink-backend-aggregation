package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping;

import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class BankIdIframeSSAuthenticationController {
    private WebDriverHelper webDriverHelper;
    private WebDriver driver;

    private static final By FORM_XPATH = By.xpath("//form");
    private static final By USERNAME_INPUT_XPATH = By.xpath("//form//input[@maxlength='11']");
    private static final By PASSWORD_INPUT_XPATH =
            By.xpath("//form//input[@type='password'][@maxlength]");
    private static final By AUTHENTICATION_LIST_BUTTON_XPATH =
            By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");
    private static final By BANK_ID_MOBIL_BUTTON =
            By.xpath(
                    "//ul/child::li/child::button[span[contains(text(),'mobil') and contains(text(),'BankID')]]");
    private static final int WAIT_RENDER_MILLIS = 1000;

    public BankIdIframeSSAuthenticationController(
            WebDriverHelper webDriverHelper, WebDriver driver) {
        this.webDriverHelper = webDriverHelper;
        this.driver = driver;
    }

    public void doLogin(String username, String password) throws AuthenticationException {
        webDriverHelper.switchToIframe(driver);

        submitUsername(driver, username);

        getListAuthenticationMethods(driver);

        chooseBankIdMobil(driver);

        waitForUserInteractionAndSendBankIdPassword(driver, password);
    }

    private void waitForUserInteractionAndSendBankIdPassword(WebDriver driver, String password)
            throws LoginException {
        WebElement bankIdPasswordInputElement = waitForUserInteraction(driver);
        sendValueToInputAndSubmit(bankIdPasswordInputElement, password);
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
        webDriverHelper.submitForm(driver, FORM_XPATH);
    }

    private void submitUsername(WebDriver driver, String username) {
        WebElement userInput = webDriverHelper.getElement(driver, USERNAME_INPUT_XPATH);
        sendValueToInputAndSubmit(userInput, username);
    }

    private void getLoadedBankIdIframe(WebDriver driver) {
        getBankIdLoginIframe(driver);
        checkIfLoadedIfNotRefresh(driver);
    }

    private boolean checkIfLoadedIfNotRefresh(WebDriver driver) {
        for (int i = 0; i < 10; i++) {
            if (isErrorWhenBankIdLoaded(driver)) {
                getBankIdLoginIframe(driver);
                webDriverHelper.sleep(WAIT_RENDER_MILLIS * i);
            } else {
                return true;
            }
        }
        throw new ScreenScrapingException("Bank Id template not loaded");
    }

    private void getBankIdLoginIframe(WebDriver driver) {
        driver.get(loginBaseUrl);
        webDriverHelper.switchToIframe(driver);
    }

    private boolean isErrorWhenBankIdLoaded(WebDriver driver) {
        return driver.findElements(USERNAME_INPUT_XPATH).isEmpty();
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

    private void sendValueToInputAndSubmit(WebElement input, String value) {
        webDriverHelper.sendInputValue(input, value);
        webDriverHelper.submitForm(driver, FORM_XPATH);
    }
}

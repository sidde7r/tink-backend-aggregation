package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class BankIdIframeInitializer implements IframeInitializer {
    private final String username;
    private final WebDriver driver;
    private final WebDriverHelper webDriverHelper;

    private static final By USERNAME_XPATH = By.xpath("//form//input[@maxlength='11']");
    private static final By FORM_XPATH = By.xpath("//form");

    public BankIdIframeInitializer(
            String username, WebDriver driver, WebDriverHelper webDriverHelper) {
        this.username = username;
        this.driver = driver;
        this.webDriverHelper = webDriverHelper;
    }

    @Override
    public void initializeBankIdAuthentication() {
        webDriverHelper.switchToIframe(driver);
        webDriverHelper.sleep(5000);

        try {
            WebElement input = webDriverHelper.getElement(driver, USERNAME_XPATH);
            webDriverHelper.sendInputValue(input, username);
        } catch (HtmlElementNotFoundException ex) {
            throw new ScreenScrapingException("Bank Id template not loaded");
        }

        webDriverHelper.submitForm(driver, FORM_XPATH);
    }
}

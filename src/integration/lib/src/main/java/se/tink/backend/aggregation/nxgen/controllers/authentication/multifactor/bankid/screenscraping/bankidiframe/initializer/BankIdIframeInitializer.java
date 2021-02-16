package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants.Xpath;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.exceptions.HtmlElementNotFoundException;
import se.tink.integration.webdriver.exceptions.ScreenScrapingException;

public class BankIdIframeInitializer implements IframeInitializer {
    private final String username;
    private final WebDriver driver;
    private final WebDriverHelper webDriverHelper;

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
            WebElement input = webDriverHelper.getElement(driver, Xpath.USERNAME_XPATH);
            webDriverHelper.sendInputValue(input, username);
        } catch (HtmlElementNotFoundException ex) {
            throw new ScreenScrapingException("Bank Id template not loaded");
        }

        webDriverHelper.submitForm(driver, Xpath.FORM_XPATH);
    }
}

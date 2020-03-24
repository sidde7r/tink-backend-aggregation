package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankIdinitializers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.initializer.IframeInitializer;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class PortalBankIframeInitializer implements IframeInitializer {
    private final String username;
    private final WebDriver driver;
    private final WebDriverHelper webDriverHelper;

    private static final By USERNAME_XPATH =
            By.xpath("//form[@id='loginForm']//input[@maxlength='11']");
    private static final By FORM_XPATH = By.xpath("//form[@id='loginForm']");
    private static final By AUTHENTICATION_LIST_BUTTON_XPATH =
            By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");

    public PortalBankIframeInitializer(
            String username, WebDriver driver, WebDriverHelper webDriverHelper) {
        this.username = username;
        this.driver = driver;
        this.webDriverHelper = webDriverHelper;
    }

    @Override
    public void initializeBankIdAuthentication() {
        WebElement input = webDriverHelper.getElement(driver, USERNAME_XPATH);
        webDriverHelper.sendInputValue(input, username);

        webDriverHelper.submitForm(driver, FORM_XPATH);

        webDriverHelper.switchToIframe(driver);
        webDriverHelper.sleep(5000);

        try {
            webDriverHelper.getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
        } catch (HtmlElementNotFoundException ex) {
            throw new ScreenScrapingException("Bank Id template not loaded");
        }
    }
}

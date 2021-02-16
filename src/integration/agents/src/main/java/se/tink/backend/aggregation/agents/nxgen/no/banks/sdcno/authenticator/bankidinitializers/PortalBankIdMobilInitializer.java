package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

public class PortalBankIdMobilInitializer implements MobilInitializer {
    private final String mobileNummer;
    private final String nationalIdNumber;
    private final WebDriver driver;
    private final WebDriverHelper webDriverHelper;

    private static final By MOBILENUMBER_INPUT_XPATH = By.xpath("//input[@id='mobileNumber']");
    private static final By NATIONAL_ID_NUMBER_INPUT_XPATH =
            By.xpath("//input[@id='mobileCustomerId']");
    private static final By FORM_XPATH = By.xpath("//form[@id='mobileLoginForm']");

    public PortalBankIdMobilInitializer(
            String mobileNummer,
            String nationalIdNumber,
            WebDriver driver,
            WebDriverHelper webDriverHelper) {
        this.mobileNummer = mobileNummer;
        this.nationalIdNumber = nationalIdNumber;
        this.driver = driver;
        this.webDriverHelper = webDriverHelper;
    }

    @Override
    public void initializeBankIdMobilAuthentication() {
        WebElement mobileNumberInput = webDriverHelper.getElement(driver, MOBILENUMBER_INPUT_XPATH);
        webDriverHelper.sendInputValue(mobileNumberInput, mobileNummer);

        WebElement nationalIdNumberInput =
                webDriverHelper.getElement(driver, NATIONAL_ID_NUMBER_INPUT_XPATH);
        webDriverHelper.sendInputValue(nationalIdNumberInput, nationalIdNumber);

        webDriverHelper.submitForm(driver, FORM_XPATH);
    }
}

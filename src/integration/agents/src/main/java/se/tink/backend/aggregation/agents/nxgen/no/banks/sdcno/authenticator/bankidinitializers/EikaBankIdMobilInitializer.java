package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

public class EikaBankIdMobilInitializer implements MobilInitializer {
    private final String mobileNummer;
    private final String dateOfBirth;
    private final WebDriver driver;
    private final WebDriverHelper webDriverHelper;

    private static final By MOBILENUMBER_INPUT_XPATH = By.xpath("//input[@id='txtMobilnummer']");
    private static final By DATE_OF_BIRTH_INPUT_XPATH = By.xpath("//input[@id='txtFodselsdato']");
    private static final By BANK_ID_MOBIL = By.xpath("//a[@id='tab-mobil']");
    private static final By FORM_XPATH = By.xpath("//form[@id='mobileLoginForm']");

    public EikaBankIdMobilInitializer(
            String mobileNummer,
            String dateOfBirth,
            WebDriver driver,
            WebDriverHelper webDriverHelper) {
        this.mobileNummer = mobileNummer;
        this.dateOfBirth = dateOfBirth;
        this.driver = driver;
        this.webDriverHelper = webDriverHelper;
    }

    @Override
    public void initializeBankIdMobilAuthentication() {
        WebElement bankIdMobilButton = webDriverHelper.getElement(driver, BANK_ID_MOBIL);
        webDriverHelper.clickButton(bankIdMobilButton);
        webDriverHelper.sleep(1000);

        WebElement mobileNumberInput = webDriverHelper.getElement(driver, MOBILENUMBER_INPUT_XPATH);
        webDriverHelper.sendInputValue(mobileNumberInput, mobileNummer);

        WebElement nationalIdNumberInput =
                webDriverHelper.getElement(driver, DATE_OF_BIRTH_INPUT_XPATH);
        webDriverHelper.sendInputValue(nationalIdNumberInput, dateOfBirth);

        webDriverHelper.submitForm(driver, FORM_XPATH);
    }
}

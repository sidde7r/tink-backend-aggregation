package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverHelper;

public class BankIdMobilInitializer implements MobilInitializer {
    private final String mobileNumber;
    private final String dateOfBirth;
    private final WebDriver driver;
    private final WebDriverHelper webDriverHelper;

    private static final By MOBILENUMBER_INPUT_XPATH = By.xpath("//input[@id='phoneNumber']");
    private static final By DATE_OF_BIRTH_INPUT_XPATH = By.xpath("//input[@id='phoneAlias']");
    private static final By FORM_XPATH = By.xpath("//input[@id='submit_button']");

    public BankIdMobilInitializer(
            String mobileNumber,
            String dateOfBirth,
            WebDriver driver,
            WebDriverHelper webDriverHelper) {
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.driver = driver;
        this.webDriverHelper = webDriverHelper;
    }

    @Override
    public void initializeBankIdMobilAuthentication() {
        WebElement mobileNumberInput = webDriverHelper.getElement(driver, MOBILENUMBER_INPUT_XPATH);
        webDriverHelper.sendInputValue(mobileNumberInput, mobileNumber);

        WebElement dateOfBirthInput = webDriverHelper.getElement(driver, DATE_OF_BIRTH_INPUT_XPATH);
        webDriverHelper.sendInputValue(dateOfBirthInput, dateOfBirth);

        WebElement formButton = webDriverHelper.getElement(driver, FORM_XPATH);
        webDriverHelper.clickButton(formButton);
    }
}

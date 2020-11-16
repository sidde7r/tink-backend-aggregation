package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.CARD_PORTAL_PATH;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;

@Slf4j
public class PostAuthDriverProcessor {

    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final CookieManager cookieManager;

    private static final By ERROR_MESSAGE = By.id("error-message");
    private static final By ERROR_MESSAGE_CONTENT = By.xpath("//li[@role]");
    private static final By AGREEMENT_LIST = By.className("agreement-list");
    private static final By AGREEMENT_LIST_FIRST_OPTION = By.xpath("//a[@data-id='0']");

    private static final By TARGET_ELEMENT_XPATH = By.xpath("//input[@value='Logg ut']");

    public PostAuthDriverProcessor(
            WebDriver driver,
            WebDriverHelper webDriverHelper,
            TinkHttpClient client,
            SdcNoConfiguration configuration) {
        this.configuration = configuration;
        this.webDriverHelper = webDriverHelper;
        this.driver = driver;
        this.cookieManager = new CookieManager(driver, client);
    }

    public void processLogonCasesAfterSuccessfulBankIdAuthentication() {
        try {
            checkIfNotACustomer();
            checkIfMultipleAgreements();
        } catch (NoSuchElementException e) {
            // successful logon
        }
    }

    private void checkIfNotACustomer() {
        WebElement errorMessageElement = driver.findElement(ERROR_MESSAGE);
        String errorMessage = errorMessageElement.findElement(ERROR_MESSAGE_CONTENT).getText();

        if (errorMessage
                .toLowerCase()
                .contains(SdcNoConstants.ErrorMessages.NO_ACCOUNT_FOR_BANK_ID.toLowerCase())) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
    }

    private void checkIfMultipleAgreements() {
        WebElement agreementsList = driver.findElement(AGREEMENT_LIST);
        WebElement firstAgreement = agreementsList.findElement(AGREEMENT_LIST_FIRST_OPTION);
        chooseFirstAgreement(firstAgreement);
        // ITE-1457, remove after investigation
        log.info(
                "[SDC] Multiple agreements - first agreement chosen - page source: {}",
                driver.getPageSource());
    }

    private void chooseFirstAgreement(WebElement firstAgreement) {
        firstAgreement.click();
        webDriverHelper.sleep(2000);
    }

    public void processWebDriver() {
        cookieManager.setCookiesToClient();

        if (configuration.getAuthenticationType().equals(AuthenticationType.PORTAL)) {
            // loading additional website for more cookies necessary to fetch credit cards
            driver.get(configuration.getIndividualBaseURL() + CARD_PORTAL_PATH);
            try {
                webDriverHelper.waitForElement(driver, TARGET_ELEMENT_XPATH);
            } catch (HtmlElementNotFoundException ex) {
                log.info(
                        "[SDC] Credit card portal not found, for URL: {}, source: {}",
                        driver.getCurrentUrl(),
                        driver.getPageSource());
                return;
            }
            cookieManager.setCookiesToClient();
        }
    }
}

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

    private static final By ERROR_MESSAGE_CONTENT = By.xpath("//*[@id='error-message']/ul/li");
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
            checkForErrors();
            checkIfMultipleAgreements();
        } catch (NoSuchElementException e) {
            // If we do not find error element or agreement list element,
            // we know that login was successful.
        }
    }

    private void checkForErrors() {
        String errorMessage = driver.findElement(ERROR_MESSAGE_CONTENT).getAttribute("innerText");

        if (isNotACustomer(errorMessage)) {
            throw LoginError.NOT_CUSTOMER.exception();
        } else {
            log.error(
                    "[SDC] Unknown error was found - error: {}, page source: {}",
                    errorMessage,
                    driver.getPageSource());
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }

    private boolean isNotACustomer(String errorMessage) {
        return errorMessage
                .toLowerCase()
                .contains(SdcNoConstants.ErrorMessages.NO_ACCOUNT_FOR_BANK_ID.toLowerCase());
    }

    private void checkIfMultipleAgreements() {
        WebElement agreementsList = driver.findElement(AGREEMENT_LIST);
        WebElement firstAgreement = agreementsList.findElement(AGREEMENT_LIST_FIRST_OPTION);
        chooseFirstAgreement(firstAgreement);
        // ITE-1457 - we are not sure that it will work correctly
        // Probably there is a chance that we need to iterate through all agreements,
        // but for now we can choose first agreement and additionally log some information.
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

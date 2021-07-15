package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants.CARD_PORTAL_PATH;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.AuthenticationType;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config.SdcNoConstants;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.exceptions.HtmlElementNotFoundException;

@Slf4j
public class PostAuthDriverProcessor {

    private final SdcNoConfiguration configuration;
    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final CookieManager cookieManager;

    static final By ERROR_MESSAGE_CONTENT = By.xpath("//*[@id='error-message']/ul/li");
    static final By AGREEMENT_LIST = By.className("agreement-list");
    static final By AGREEMENT_LIST_FIRST_OPTION = By.xpath("//a[@data-id='0']");
    static final By ACCEPT_COOKIES_BUTTON =
            By.xpath("//button[@class='btn btn-flat' and text()='Aksepter valgte']");
    static final By POSTPONE_SURVEY_BUTTON =
            By.xpath(
                    "//button[@class='btn btn-default' and contains(@class, /span[text()='Utsett'])]");

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
        postponeAntiMoneyLaunderingSurveyIfPrompted();
        acceptCookiesIfPrompted();
        checkForErrors();
        checkIfMultipleAgreements();
    }

    private void postponeAntiMoneyLaunderingSurveyIfPrompted() {
        clickButtonAndLogIfPresent(
                POSTPONE_SURVEY_BUTTON,
                "[SDC] Clicking a button to postpone anti-money laundering survey.");
    }

    private void acceptCookiesIfPrompted() {
        clickButtonAndLogIfPresent(
                ACCEPT_COOKIES_BUTTON, "[SDC] Found cookies button. Trying to accept it.");
    }

    private void clickButtonAndLogIfPresent(By button, String s) {
        driver.findElements(button)
                .forEach(
                        element -> {
                            log.info(s);
                            element.click();
                        });
        webDriverHelper.sleep(2000);
    }

    private void checkForErrors() {
        String errorMessage =
                driver.findElements(ERROR_MESSAGE_CONTENT).stream()
                        .map(element -> element.getAttribute("innerText"))
                        .findFirst()
                        .orElse(null);
        if (errorMessage == null) {
            log.info("[SDC] Did not found any error message");
            return;
        }

        if (isNotACustomer(errorMessage)) {
            throw LoginError.NOT_CUSTOMER.exception();
        } else if (isBankError(errorMessage)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
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

    private boolean isBankError(String errorMessage) {
        return errorMessage
                .toLowerCase()
                .contains(SdcNoConstants.ErrorMessages.BANK_TEMPORARY_ERROR.toLowerCase());
    }

    private void checkIfMultipleAgreements() {
        WebElement agreementsList =
                driver.findElements(AGREEMENT_LIST).stream().findFirst().orElse(null);
        if (agreementsList == null) {
            log.info(
                    "[SDC] Did not found multiple agreements, verify page source: {}",
                    driver.getPageSource());
            return;
        }

        WebElement firstAgreement =
                agreementsList.findElements(AGREEMENT_LIST_FIRST_OPTION).stream()
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find first agreements option, verify page source: \n"
                                                        + driver.getPageSource()));
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

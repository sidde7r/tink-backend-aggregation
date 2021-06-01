package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import static io.vavr.API.$;
import static io.vavr.Predicates.instanceOf;

import io.vavr.API;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.ScaForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class BankinterAuthenticationClient {
    private final WebDriver driver;
    private final HtmlLogger htmlLogger;
    private final BankinterApiClient apiClient;

    public String login(String username, String password) {
        driver.navigate().to(Urls.LOGIN_PAGE);
        htmlLogger.info("Logging in to " + Urls.LOGIN_PAGE);
        final WebElement loginForm = findWebElement(driver, By.id(LoginForm.FORM_ID));
        final WebElement nameField = findWebElement(driver, By.id(LoginForm.USERNAME_FIELD));
        final WebElement passwordField = findWebElement(driver, By.id(LoginForm.PASSWORD_FIELD));
        nameField.sendKeys(username);
        passwordField.sendKeys(password);
        String loginUrl = driver.getCurrentUrl();
        loginForm.submit();
        return loginUrl;
    }

    public boolean isScaNeeded() {
        String currentUrl = driver.getCurrentUrl();
        return URL.of(currentUrl).toUri().getPath().equalsIgnoreCase(Paths.VERIFY_SCA);
    }

    public String submitSca(SupplementalInformationHelper supplementalInformationHelper) {
        try {
            final WebElement codeField =
                    findWebElement(driver, By.cssSelector(ScaForm.CODE_FIELD_SELECTOR));
            final WebElement submitButton =
                    findWebElement(driver, By.cssSelector(ScaForm.SUBMIT_BUTTON_SELECTOR));
            String code = supplementalInformationHelper.waitForOtpInput();
            codeField.sendKeys(code);
            String scaUrl = driver.getCurrentUrl();
            submitButton.click();
            return scaUrl;
        } catch (SupplementalInfoException e) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
        }
    }

    public void finishProcess() {
        if (isShowingError()) {
            htmlLogger.error("Login has failed");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (URL.of(driver.getCurrentUrl())
                .toUri()
                .getPath()
                .equalsIgnoreCase(Paths.GLOBAL_POSITION)) {
            apiClient.storeLoginCookies(driver.manage().getCookies());
            driver.quit();
            return;
        }
        log.error("Did not reach logged in state or error message: " + driver.getCurrentUrl());
        htmlLogger.error("Did not reach logged");
        throw LoginError.NOT_SUPPORTED.exception();
    }

    public void waitForErrorOrRedirect(long timeoutSeconds, String previousUrl) {
        new WebDriverWait(driver, timeoutSeconds)
                .ignoring(StaleElementReferenceException.class)
                .until(
                        webDriver -> {
                            if (!webDriver.getCurrentUrl().equals(previousUrl)) {
                                return true;
                            }
                            return isShowingError();
                        });
    }

    private boolean isShowingError() {
        try {
            return findWebElement(driver, By.id(LoginForm.ERROR_PANEL)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private WebElement findWebElement(SearchContext searchContext, By query) {
        return Try.of(() -> searchContext.findElement(query))
                .mapFailure(
                        API.Case(
                                $(instanceOf(NoSuchElementException.class)),
                                ex -> {
                                    htmlLogger.error(
                                            String.format(
                                                    "Could not find an element `%s`",
                                                    query.toString()));
                                    return ex;
                                }))
                .get();
    }
}

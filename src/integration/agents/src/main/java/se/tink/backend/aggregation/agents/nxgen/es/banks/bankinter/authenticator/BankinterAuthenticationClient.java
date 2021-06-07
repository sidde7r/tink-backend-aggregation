package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.ScaForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page.AttemptsLimitExceededException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page.Field;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page.Page;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class BankinterAuthenticationClient {
    private final WebDriver driver;
    private final HtmlLogger htmlLogger;
    private final BankinterApiClient apiClient;

    public void login(String username, String password) throws AttemptsLimitExceededException {
        driver.navigate().to(Urls.LOGIN_PAGE);
        htmlLogger.info("Logging in to " + Urls.LOGIN_PAGE);
        Page.of(driver, htmlLogger)
                .fields(
                        Field.of(
                                By.id(LoginForm.USERNAME_FIELD), field -> field.sendKeys(username)),
                        Field.of(
                                By.id(LoginForm.PASSWORD_FIELD), field -> field.sendKeys(password)))
                .submit(() -> By.id(LoginForm.FORM_ID), LoginForm.SUBMIT_TIMEOUT_SECONDS);
    }

    public boolean isScaNeeded() {
        String currentUrl = driver.getCurrentUrl();
        return URL.of(currentUrl).toUri().getPath().equalsIgnoreCase(Paths.VERIFY_SCA);
    }

    public void submitSca(SupplementalInformationHelper supplementalInformationHelper)
            throws AttemptsLimitExceededException {
        try {
            String code = supplementalInformationHelper.waitForOtpInput();
            Page.of(driver, htmlLogger)
                    .fields(
                            Field.of(
                                    By.cssSelector(ScaForm.CODE_FIELD_SELECTOR),
                                    field -> field.sendKeys(code)))
                    .click(
                            () -> By.cssSelector(ScaForm.SUBMIT_BUTTON_SELECTOR),
                            ScaForm.SUBMIT_TIMEOUT_SECONDS);
        } catch (SupplementalInfoException e) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
        }
    }

    public void finishProcess() {
        if (Page.of(driver, htmlLogger).isError()) {
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
}

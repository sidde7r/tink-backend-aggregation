package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;

@Slf4j
public class BankIdMobilSSAuthenticationController {
    private WebDriverHelper webDriverHelper;
    private WebDriver driver;
    private MobilInitializer bankIdMobilInitializer;

    private static final By WAITING_FOR_AUTHENTICATION_ELEMENT_XPATH =
            By.xpath("//*[contains(text(),'Referanse')]");
    private static final int WAIT_RENDER_MILLIS = 1000;

    public BankIdMobilSSAuthenticationController(
            MobilInitializer bankIdMobilInitializer,
            WebDriver driver,
            WebDriverHelper driverHelper) {
        this.webDriverHelper = driverHelper;
        this.driver = driver;
        this.bankIdMobilInitializer = bankIdMobilInitializer;
    }

    public void doLogin() throws AuthenticationException {

        bankIdMobilInitializer.initializeBankIdMobilAuthentication();

        try {
            // referrence words which defines waiting for authentication appears for a moment after
            // login site. Needs additional waiting for JS to finish on bank site
            webDriverHelper.sleep(2000);

            webDriverHelper.getElement(driver, WAITING_FOR_AUTHENTICATION_ELEMENT_XPATH);
        } catch (HtmlElementNotFoundException e) {
            throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception(
                    "User provided invalid credentials or bank Id by mobile is not activated");
        }

        if (!isUserAuthenticated(driver)) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                    "User did not accept bank id");
        }
    }

    private boolean isUserAuthenticated(WebDriver driver) {
        log.info(
                "Waiting for user to accept bank Id in mobile app, current browser content: {}",
                driver.getPageSource());

        for (int i = 0; i < 90; i++) {
            webDriverHelper.sleep(WAIT_RENDER_MILLIS);
            if (driver.findElements(WAITING_FOR_AUTHENTICATION_ELEMENT_XPATH).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}

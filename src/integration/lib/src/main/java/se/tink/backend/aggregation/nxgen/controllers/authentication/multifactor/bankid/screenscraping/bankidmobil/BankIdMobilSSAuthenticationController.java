package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RequiredArgsConstructor
public class BankIdMobilSSAuthenticationController {
    private static final By WAITING_FOR_AUTHENTICATION_ELEMENT_XPATH =
            By.xpath("//*[contains(text(),'Referanse')]");
    private static final By REFERENCE_NUMBER = By.className("ref_nr");
    private static final LocalizableKey VALIDATE_REFERENCE_NUMBER =
            new LocalizableKey("Please check if reference number matches with SIM message");
    private static final int WAIT_RENDER_MILLIS = 1000;

    private final WebDriverHelper webDriverHelper;
    private final WebDriver driver;
    private final MobilInitializer bankIdMobilInitializer;
    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;

    public void doLogin(Credentials credentials) throws AuthenticationException {

        bankIdMobilInitializer.initializeBankIdMobilAuthentication();

        try {
            // referrence words which defines waiting for authentication appears for a moment after
            // login site. Needs additional waiting for JS to finish on bank site
            webDriverHelper.sleep(2000);

            webDriverHelper.getElement(driver, WAITING_FOR_AUTHENTICATION_ELEMENT_XPATH);
        } catch (HtmlElementNotFoundException e) {
            log.info(
                    "BankID Mobile not supported (no reference code on page), see source: {}",
                    driver.getPageSource());
            throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception(
                    "User provided invalid credentials or bank Id by mobile is not activated");
        }

        if (!isUserAuthenticated(credentials)) {
            throw BankIdError.TIMEOUT.exception();
        }

        // ITE-1457, remove after investigation
        log.info("User is supposed to be authenticated. Page source: {}", driver.getPageSource());
    }

    private boolean isUserAuthenticated(Credentials credentials) {
        log.info("Waiting for user to accept bank Id in mobile app");

        displayPrompt(credentials);

        for (int i = 0; i < 90; i++) {
            webDriverHelper.sleep(WAIT_RENDER_MILLIS);
            if (driver.findElements(WAITING_FOR_AUTHENTICATION_ELEMENT_XPATH).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void displayPrompt(Credentials credentials) {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description(catalog.getString(VALIDATE_REFERENCE_NUMBER))
                        .value(getReferenceNumber())
                        .name("reference_number")
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, true);
    }

    private String getReferenceNumber() {
        return webDriverHelper
                .waitForElement(driver, REFERENCE_NUMBER)
                .map(WebElement::getText)
                .orElse(null);
    }
}

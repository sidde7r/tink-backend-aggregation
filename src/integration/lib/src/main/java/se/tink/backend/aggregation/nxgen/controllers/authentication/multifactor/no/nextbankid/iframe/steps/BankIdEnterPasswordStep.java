package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_PRIVATE_PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdEnterPasswordStep {

    private final BankIdWebDriver webDriver;

    public void enterPrivatePassword(Credentials credentials) {
        waitForPasswordInput();
        enterPassword(credentials);
        clickNext();
    }

    private void waitForPasswordInput() {
        log.info("{} Waiting for private password input", BANK_ID_LOG_PREFIX);
        boolean inputFound =
                webDriver
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
                                        .searchFor(LOC_PRIVATE_PASSWORD_INPUT)
                                        .searchForSeconds(10)
                                        .build())
                        .isNotEmpty();
        if (!inputFound) {
            throw new IllegalStateException("Cannot find password input");
        }
        /*
        Additional wait to assure input element is interactable - typically it's sufficient to check if element
         .isDisplayed() but in this case it doesn't work.
         */
        webDriver.sleepFor(1_000);
    }

    private void enterPassword(Credentials credentials) {
        log.info("{} Entering private password", BANK_ID_LOG_PREFIX);
        webDriver.setValueToElement(
                credentials.getField(Field.Key.BANKID_PASSWORD), LOC_PRIVATE_PASSWORD_INPUT);
    }

    private void clickNext() {
        log.info("{} Submitting private password", BANK_ID_LOG_PREFIX);
        webDriver.clickButton(LOC_SUBMIT_BUTTON);
    }
}

package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHIP_CODE_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_SSN_SCREEN;

import com.google.inject.Inject;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAuthWithChipCodeStep {

    private static final Pattern VALID_CHIP_CODE_FORMAT_REGEX = Pattern.compile("^\\d{6}$");

    private final BankIdWebDriver webDriver;
    private final BankIdScreensManager screensManager;

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticateWithChipCode() {
        String code = askUserForCode();
        verifyCodeIsValid(code);

        enterCode(code);
        clickNext();

        verifyChipCodeResult();
    }

    private String askUserForCode() {
        log.info("{} Asking user for chip code", BANK_ID_LOG_PREFIX);

        Field chipCodeField = NorwegianFields.BankIdCodeChipField.build(catalog);

        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(chipCodeField);

        return supplementalInfoResponse.get(chipCodeField.getName());
    }

    private void verifyCodeIsValid(String code) {
        if (!VALID_CHIP_CODE_FORMAT_REGEX.matcher(code).matches()) {
            throw new IllegalStateException("Incorrect format for chip code: " + code);
        }
        log.info("{} Chip code has correct format", BANK_ID_LOG_PREFIX);
    }

    private void enterCode(String number) {
        log.info("{} Entering chip code", BANK_ID_LOG_PREFIX);
        webDriver.setValueToElement(number, LOC_CHIP_CODE_INPUT);
    }

    private void clickNext() {
        log.info("{} Clicking submit chip code button", BANK_ID_LOG_PREFIX);
        webDriver.clickButton(LOC_SUBMIT_BUTTON);
    }

    private void verifyChipCodeResult() {
        log.info("{} Waiting for SSN screen after submitting chip code", BANK_ID_LOG_PREFIX);
        BankIdScreen bankIdScreen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN, ENTER_SSN_SCREEN)
                                .waitForSeconds(10)
                                .verifyNoErrorScreens(true)
                                .build());

        if (bankIdScreen == ENTER_BANK_ID_PASSWORD_SCREEN) {
            // we've successfully reached next authentication step
            log.info("{} Chip code valid", BANK_ID_LOG_PREFIX);
            return;
        }

        // when we're moved back to enter SSN screen it's either invalid SSN or invalid chip code
        if (bankIdScreen == ENTER_SSN_SCREEN) {
            throw BankIdNOError.INVALID_SSN_OR_CHIP_CODE.exception();
        }
    }
}

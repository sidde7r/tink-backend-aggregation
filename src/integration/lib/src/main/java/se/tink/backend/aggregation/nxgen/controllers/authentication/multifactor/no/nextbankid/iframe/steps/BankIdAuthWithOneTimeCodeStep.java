package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_ONE_TIME_CODE_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.Validation.VALID_ONE_TIME_CODE_PATTERN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_SSN_SCREEN;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAuthWithOneTimeCodeStep {

    private final WebDriverService webDriver;
    private final BankIdScreensManager screensManager;
    private final BankIdAuthenticationState authenticationState;

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticateWithOneTimeCode() {
        String code = askUserForCode();
        verifyCodeIsValid(code);

        enterCode(code);
        clickNext();

        verifyOneTimeCodeResult();
    }

    private String askUserForCode() {
        log.info("{} Asking user for one-time code", BANK_ID_LOG_PREFIX);

        Field codeField = NorwegianFields.BankIdOneTimeCodeField.build(catalog);

        Map<String, String> supplementalInfoResponse =
                supplementalInformationController.askSupplementalInformationSync(codeField);

        return supplementalInfoResponse.get(codeField.getName());
    }

    private void verifyCodeIsValid(String code) {
        boolean isValid =
                Optional.ofNullable(code)
                        .map(VALID_ONE_TIME_CODE_PATTERN::matcher)
                        .map(Matcher::matches)
                        .orElse(false);
        if (!isValid) {
            log.error("{} Invalid OTP code format: {}", BANK_ID_LOG_PREFIX, code);
            throw BankIdNOError.INVALID_ONE_TIME_CODE_FORMAT.exception();
        }
        log.info("{} One-time code has correct format", BANK_ID_LOG_PREFIX);
    }

    private void enterCode(String number) {
        log.info("{} Entering one-time code", BANK_ID_LOG_PREFIX);
        webDriver.setValueToElement(number, LOC_ONE_TIME_CODE_INPUT);
    }

    private void clickNext() {
        log.info("{} Clicking submit one-time code button", BANK_ID_LOG_PREFIX);
        webDriver.clickButton(LOC_SUBMIT_BUTTON);
    }

    private void verifyOneTimeCodeResult() {
        log.info("{} Waiting for SSN screen after submitting one-time code", BANK_ID_LOG_PREFIX);
        BankIdScreen bankIdScreen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN, ENTER_SSN_SCREEN)
                                .waitForSeconds(10)
                                .verifyNoErrorScreens(true)
                                .build());

        if (bankIdScreen == ENTER_BANK_ID_PASSWORD_SCREEN) {
            // we've successfully reached next authentication step
            log.info("{} One-time code valid", BANK_ID_LOG_PREFIX);
            return;
        }

        if (bankIdScreen == ENTER_SSN_SCREEN) {
            if (authenticationState.getFirstIframeWindow() == BankIdIframeFirstWindow.ENTER_SSN) {
                // when 1st iframe screen was about entering SSN and we're back on it it's either
                // invalid SSN or invalid one-time code
                throw BankIdNOError.INVALID_SSN_OR_ONE_TIME_CODE.exception();
            }
            // otherwise it must be invalid OTP
            throw BankIdNOError.INVALID_ONE_TIME_CODE.exception();
        }
    }
}

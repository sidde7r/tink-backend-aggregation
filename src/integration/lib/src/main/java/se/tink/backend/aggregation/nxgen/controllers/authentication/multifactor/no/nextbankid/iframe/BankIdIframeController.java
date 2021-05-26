package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.Validation.VALID_PASSWORD_PATTERN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.Validation.VALID_SSN_PATTERN;

import com.google.inject.Inject;
import java.util.Optional;
import java.util.regex.Matcher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterPasswordStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterSSNStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdPerform2FAStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdVerifyAuthenticationStep;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class BankIdIframeController {

    private final BankIdAuthenticationState authenticationState;

    private final BankIdEnterSSNStep enterSSNStep;
    private final BankIdPerform2FAStep perform2FAStep;
    private final BankIdEnterPasswordStep enterPrivatePasswordStep;
    private final BankIdVerifyAuthenticationStep verifyAuthenticationStep;

    public void authenticateWithCredentials(Credentials credentials) {
        String ssn = credentials.getField(Field.Key.USERNAME);
        validateSSN(ssn);

        String password = credentials.getField(Field.Key.BANKID_PASSWORD);
        validatePassword(password);

        BankIdIframeFirstWindow firstWindow = authenticationState.getFirstIframeWindow();

        log.info(
                "{} Starting iframe authentication with window: {}",
                BANK_ID_LOG_PREFIX,
                firstWindow);

        log.info(
                "{} Starting iframe authentication with window: {}",
                BANK_ID_LOG_PREFIX,
                firstWindow);

        if (firstWindow == BankIdIframeFirstWindow.ENTER_SSN) {
            enterSSNStep.enterSSN(ssn);
            log.info("{} SSN entered successfully", BANK_ID_LOG_PREFIX);
        }

        perform2FAStep.perform2FA();
        log.info("{} 2FA completed", BANK_ID_LOG_PREFIX);

        enterPrivatePasswordStep.enterPrivatePassword(password);
        log.info("{} Private password entered", BANK_ID_LOG_PREFIX);

        verifyAuthenticationStep.verify();
        log.info("{} Iframe authentication finished", BANK_ID_LOG_PREFIX);
    }

    private void validateSSN(String ssn) {
        boolean isValid =
                Optional.ofNullable(ssn)
                        .map(VALID_SSN_PATTERN::matcher)
                        .map(Matcher::matches)
                        .orElse(false);
        if (!isValid) {
            log.error("{} Invalid SSN format: {}", BANK_ID_LOG_PREFIX, ssn);
            throw BankIdNOError.INVALID_SSN_FORMAT.exception();
        }
    }

    private void validatePassword(String password) {
        boolean isValid =
                Optional.ofNullable(password)
                        .map(VALID_PASSWORD_PATTERN::matcher)
                        .map(Matcher::matches)
                        .orElse(false);
        if (!isValid) {
            throw BankIdNOError.INVALID_BANK_ID_PASSWORD_FORMAT.exception();
        }
    }
}

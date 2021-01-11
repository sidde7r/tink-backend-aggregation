package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.ENTER_ACTIVATION_PASSWORD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.INCORRECT_CREDENTIALS_ERROR_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_APP_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_METHOD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_CREDENTIALS_VALIDATION_ELEMENTS_METRIC;

import java.util.Optional;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.pair.Pair;

@Slf4j
@RequiredArgsConstructor
public class NemIdVerifyLoginResponseStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final NemIdTokenValidator nemIdTokenValidator;

    public void validateLoginResponse(Credentials credentials) {
        metrics.executeWithTimer(
                this::verifyCorrectLoginResponse,
                WAITING_FOR_CREDENTIALS_VALIDATION_ELEMENTS_METRIC);

        log.info("{} Provided credentials are valid", NEM_ID_PREFIX);
        statusUpdater.updateStatusPayload(credentials, UserMessage.VALID_CREDS);
    }

    private void verifyCorrectLoginResponse() {
        Pair<By, WebElement> validationElementsSearchResult =
                waitForFirstCredentialsValidationElement(
                        NEMID_TOKEN,
                        NEMID_CODE_APP_METHOD,
                        NEMID_CODE_CARD_METHOD,
                        NEMID_CODE_TOKEN_METHOD,
                        NOT_EMPTY_ERROR_MESSAGE);

        By elementSelector = validationElementsSearchResult.first;
        WebElement element = validationElementsSearchResult.second;

        if (elementSelector == NEMID_CODE_APP_METHOD) {
            // code app 2FA method is ready to use
            return;
        }
        if (elementSelector == NEMID_TOKEN) {
            /*
            When NemId token is present it means that our custom JavaScript has received an event from NemId iframe
            by window.parent.postMessage. This in turn means that NemId iframe has handed over control to us
            and considers authentication process as finished.
            Since we didn't even sent 2FA request the received token must indicate authentication failure.
            */
            nemIdTokenValidator.throwInvalidTokenExceptionWithoutValidation(element.getText());
        }
        if (elementSelector == NEMID_CODE_CARD_METHOD) {
            throwUnsupportedCodeCardMethodException();
        }
        if (elementSelector == NEMID_CODE_TOKEN_METHOD) {
            throwUnsupportedCodeTokenMethodException();
        }
        if (elementSelector == NOT_EMPTY_ERROR_MESSAGE) {
            throwCredentialsValidationError(element.getText());
        }

        log.error(
                "{} Can't validate NemId credentials, please verify page source: {}",
                NEM_ID_PREFIX,
                driverWrapper.getFullPageSourceLog());
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't validate NemId credentials.");
    }

    private Pair<By, WebElement> waitForFirstCredentialsValidationElement(By... byArray) {
        for (int i = 0; i < 30; i++) {

            Optional<Pair<By, WebElement>> maybeFindResult =
                    driverWrapper.searchForFirstElement(byArray);
            if (maybeFindResult.isPresent()) {
                return maybeFindResult.get();
            }

            driverWrapper.sleepFor(1_000);
        }
        return Pair.of(null, null);
    }

    private void throwUnsupportedCodeCardMethodException() {
        throw NemIdError.CODEAPP_NOT_REGISTERED.exception(NEM_ID_PREFIX + " User has code card.");
    }

    private void throwUnsupportedCodeTokenMethodException() {
        throw NemIdError.CODEAPP_NOT_REGISTERED.exception(NEM_ID_PREFIX + " User has code token.");
    }

    private void throwCredentialsValidationError(String errorText) throws LoginException {
        log.error("{} NemID credentials validation error: {}", NEM_ID_PREFIX, errorText);

        String err = errorText.toLowerCase();

        if (INCORRECT_CREDENTIALS_ERROR_PATTERNS.stream()
                .map(p -> p.matcher(err))
                .anyMatch(Matcher::matches)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(NEM_ID_PREFIX + err);
        }
        if (ENTER_ACTIVATION_PASSWORD.equalsIgnoreCase(err)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    new LocalizableKey(ENTER_ACTIVATION_PASSWORD));
        }
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Unknown login error: " + errorText);
    }
}

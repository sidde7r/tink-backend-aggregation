package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.ENTER_6_DIGIT_PASSWORD_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.ENTER_ACTIVATION_PASSWORD_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.INCORRECT_CREDENTIALS_ERROR_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.KEY_APP_NOT_READY_TO_USE_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.NEMID_ISSUES_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.NEM_ID_RENEW_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.NEM_ID_REVOKED_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.USE_NEW_CODE_CARD_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_WIDE_INFO_HEADING;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_CREDENTIALS_VALIDATION_ELEMENTS_METRIC;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsStatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdVerifyLoginResponseStep {

    public static final List<By> ELEMENTS_TO_SEARCH_FOR_IN_IFRAME =
            ImmutableList.<By>builder()
                    .addAll(NemId2FAMethodScreen.getSelectorsForAllScreens())
                    .add(NOT_EMPTY_ERROR_MESSAGE)
                    .add(NEMID_WIDE_INFO_HEADING)
                    .build();

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdCredentialsStatusUpdater statusUpdater;
    private final NemIdTokenValidator nemIdTokenValidator;

    public NemId2FAMethodScreen checkLoginResultAndGetDefault2FAScreen(Credentials credentials) {
        NemId2FAMethodScreen nemId2FAMethodScreen =
                metrics.executeWithTimer(
                        this::verifyCorrectLoginResponseAndGetDefault2FAScreen,
                        WAITING_FOR_CREDENTIALS_VALIDATION_ELEMENTS_METRIC);

        log.info("{} Provided credentials are valid", NEM_ID_PREFIX);
        statusUpdater.updateStatusPayload(credentials, UserMessage.VALID_CREDS);

        return nemId2FAMethodScreen;
    }

    private NemId2FAMethodScreen verifyCorrectLoginResponseAndGetDefault2FAScreen() {
        ElementsSearchResult validationElementsSearchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(ELEMENTS_TO_SEARCH_FOR_IN_IFRAME)
                                .searchForSeconds(30)
                                .build());

        By elementSelector = validationElementsSearchResult.getSelector();
        WebElement element = validationElementsSearchResult.getWebElement();

        Optional<NemId2FAMethodScreen> maybe2FAScreen =
                NemId2FAMethodScreen.getScreenBySelector(elementSelector);
        if (maybe2FAScreen.isPresent()) {
            // some 2FA method is ready to use
            NemId2FAMethodScreen nemId2FAMethodScreen = maybe2FAScreen.get();
            log.info(
                    "{}[NemIdVerifyLoginResponseStep] Some 2FA method ready to use {}",
                    NEM_ID_PREFIX,
                    nemId2FAMethodScreen.getSupportedMethod().getUserFriendlyName().get());
            return nemId2FAMethodScreen;
        }
        if (elementSelector == NOT_EMPTY_NEMID_TOKEN) {
            /*
            When NemId token is present it means that our custom JavaScript has received an event from NemId iframe
            by window.parent.postMessage. This in turn means that NemId iframe has handed over control to us
            and considers authentication process as finished.
            Since we didn't even sent 2FA request the received token must indicate authentication failure.
            */
            nemIdTokenValidator.throwInvalidTokenExceptionWithoutValidation(element.getText());
        }
        if (elementSelector == NOT_EMPTY_ERROR_MESSAGE) {
            throwCredentialsValidationError(element.getText());
        }
        if (elementSelector == NEMID_WIDE_INFO_HEADING) {
            throwNemIdError(element.getText());
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't validate NemId credentials.");
    }

    private void throwCredentialsValidationError(String errorText) throws LoginException {
        String errorTextLowerCase = errorText.toLowerCase();

        if (valueMatchesAnyPattern(errorTextLowerCase, INCORRECT_CREDENTIALS_ERROR_PATTERNS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(NEM_ID_PREFIX + errorTextLowerCase);
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, ENTER_ACTIVATION_PASSWORD_PATTERNS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    UserMessage.ENTER_ACTIVATION_PASSWORD.getKey());
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Unknown login error: " + errorText);
    }

    private void throwNemIdError(String errorText) throws LoginException {
        String errorTextLowerCase = errorText.toLowerCase();

        if (valueMatchesAnyPattern(errorTextLowerCase, NEM_ID_REVOKED_PATTERNS)) {
            throw NemIdError.NEMID_BLOCKED.exception();
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, NEM_ID_RENEW_PATTERNS)) {
            throw NemIdError.RENEW_NEMID.exception();
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, USE_NEW_CODE_CARD_PATTERNS)) {
            throw NemIdError.USE_NEW_CODE_CARD.exception();
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, KEY_APP_NOT_READY_TO_USE_PATTERNS)) {
            throw NemIdError.KEY_APP_NOT_READY_TO_USE.exception();
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, NEMID_ISSUES_PATTERNS)) {
            throw LoginError.ACTIVATION_TIMED_OUT.exception();
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, ENTER_ACTIVATION_PASSWORD_PATTERNS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    UserMessage.ENTER_ACTIVATION_PASSWORD.getKey());
        }

        if (valueMatchesAnyPattern(errorTextLowerCase, ENTER_6_DIGIT_PASSWORD_PATTERNS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    UserMessage.ENTER_6_DIGIT_CODE.getKey());
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Unknown NemId error: " + errorText);
    }

    private boolean valueMatchesAnyPattern(String value, List<Pattern> patterns) {
        return patterns.stream().map(p -> p.matcher(value)).anyMatch(Matcher::matches);
    }
}

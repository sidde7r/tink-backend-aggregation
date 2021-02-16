package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.INVALID_CODE_CARD_CODE_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_CARD_CODE_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdCodeCardGetTokenStep {

    private static final int WAIT_FOR_TOKEN_TIMEOUT_IN_SECONDS = 10;

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;

    public String enterCodeAndGetToken(String codeCardCode) {
        return metrics.executeWithTimer(
                () -> {
                    enterCode(codeCardCode);
                    clickSendButton();

                    return findNemIdToken();
                },
                WAITING_FOR_SUPPLEMENTAL_INFO_METRIC);
    }

    private void enterCode(String code) {
        driverWrapper.setValueToElement(code, NEMID_CODE_CARD_CODE_INPUT);
    }

    private void clickSendButton() {
        driverWrapper.clickButton(SUBMIT_BUTTON);
    }

    private String findNemIdToken() {
        for (int i = 0; i < WAIT_FOR_TOKEN_TIMEOUT_IN_SECONDS; i++) {

            driverWrapper.switchToParentWindow();
            Optional<String> maybeNemIdToken = tryFindNotEmptyNemIdToken();

            if (maybeNemIdToken.isPresent()) {
                return maybeNemIdToken.get();
            }

            driverWrapper.trySwitchToNemIdIframe();
            checkForErrorMessage();

            driverWrapper.sleepFor(1_000);
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't find NemId token");
    }

    private Optional<String> tryFindNotEmptyNemIdToken() {
        return driverWrapper
                .tryFindElement(NEMID_TOKEN)
                .map(WebElement::getText)
                .map(Strings::emptyToNull);
    }

    private void checkForErrorMessage() {
        String errorMessage =
                driverWrapper
                        .tryFindElement(NOT_EMPTY_ERROR_MESSAGE)
                        .map(WebElement::getText)
                        .orElse(null);
        if (errorMessage == null) {
            return;
        }

        String errorMessageLowerCase = errorMessage.toLowerCase();
        if (valueMatchesAnyPattern(errorMessageLowerCase, INVALID_CODE_CARD_CODE_PATTERNS)) {
            throw NemIdError.INVALID_CODE_CARD_CODE.exception();
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Unknown code card code error message: " + errorMessage);
    }

    private boolean valueMatchesAnyPattern(String value, List<Pattern> patterns) {
        return patterns.stream().map(p -> p.matcher(value)).anyMatch(Matcher::matches);
    }
}

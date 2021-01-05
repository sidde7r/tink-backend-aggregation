package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_TIMEOUT_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetricLabel.WAITING_FOR_TOKEN_METRIC;

import com.google.common.base.Strings;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdTokenValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.metrics.NemIdMetrics;

@Slf4j
@RequiredArgsConstructor
public class NemIdCollectTokenStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;
    private final NemIdTokenValidator nemIdTokenValidator;

    public String collectToken() {
        String token =
                metrics.executeWithTimer(this::waitForNotEmptyToken, WAITING_FOR_TOKEN_METRIC);
        nemIdTokenValidator.verifyTokenIsValid(token);
        return token;
    }

    private String waitForNotEmptyToken() {

        for (int i = 0; i < NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN; i++) {

            driverWrapper.switchToParentWindow();
            Optional<String> maybeNemIdToken = tryFindNotEmptyNemIdToken();
            if (maybeNemIdToken.isPresent()) {
                return maybeNemIdToken.get();
            }

            if (driverWrapper.trySwitchToNemIdIframe()) {
                Optional<WebElement> maybeTimeoutIcon = tryFindNemIdTimeoutIcon();
                if (maybeTimeoutIcon.isPresent()) {
                    throw NemIdError.TIMEOUT.exception();
                }
            }

            driverWrapper.sleepFor(1_000);
        }

        log.error(
                "{} Can't find NemId token, please verify page source: {}",
                NEM_ID_PREFIX,
                driverWrapper.getFullPageSourceLog());
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't find NemId token");
    }

    private Optional<String> tryFindNotEmptyNemIdToken() {
        return driverWrapper
                .tryFindElement(NEMID_TOKEN)
                .map(WebElement::getText)
                .map(Strings::emptyToNull);
    }

    private Optional<WebElement> tryFindNemIdTimeoutIcon() {
        return driverWrapper.tryFindElement(NEMID_TIMEOUT_ICON);
    }
}

package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TIMEOUT_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_TOKEN_METRIC;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdCodeAppCollectTokenStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;

    public String collectToken() {
        return metrics.executeWithTimer(this::waitForNotEmptyToken, WAITING_FOR_TOKEN_METRIC);
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

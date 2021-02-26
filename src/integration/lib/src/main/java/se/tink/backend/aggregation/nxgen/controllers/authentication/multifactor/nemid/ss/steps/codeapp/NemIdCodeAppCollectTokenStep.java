package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_TIMEOUT_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_TOKEN_METRIC;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
class NemIdCodeAppCollectTokenStep {

    private final NemIdWebDriverWrapper driverWrapper;
    private final NemIdMetrics metrics;

    public String collectToken() {
        return metrics.executeWithTimer(this::waitForNotEmptyToken, WAITING_FOR_TOKEN_METRIC);
    }

    private String waitForNotEmptyToken() {
        ElementsSearchResult searchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(NEMID_TIMEOUT_ICON)
                                .searchForSeconds(
                                        NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN)
                                .build());
        By elementSelector = searchResult.getSelector();

        if (elementSelector == NOT_EMPTY_NEMID_TOKEN) {
            return searchResult.getElementTextTrimmed();
        }
        if (elementSelector == NEMID_TIMEOUT_ICON) {
            throw NemIdError.TIMEOUT.exception();
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't find NemId token for code app token authentication");
    }
}

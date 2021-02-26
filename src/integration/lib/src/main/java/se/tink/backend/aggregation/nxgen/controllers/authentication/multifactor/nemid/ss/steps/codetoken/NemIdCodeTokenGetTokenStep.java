package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.Errors.INVALID_CARD_OR_TOKEN_CODE_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NEMID_CODE_TOKEN_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.NOT_EMPTY_NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetricLabel.WAITING_FOR_SUPPLEMENTAL_INFO_METRIC;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.ElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils.NemIdWebDriverWrapper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
class NemIdCodeTokenGetTokenStep {

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
        driverWrapper.setValueToElement(code, NEMID_CODE_TOKEN_INPUT);
    }

    private void clickSendButton() {
        driverWrapper.clickButton(SUBMIT_BUTTON);
    }

    private String findNemIdToken() {
        ElementsSearchResult searchResult =
                driverWrapper.searchForFirstElement(
                        ElementsSearchQuery.builder()
                                .searchInParentWindow(NOT_EMPTY_NEMID_TOKEN)
                                .searchInAnIframe(NOT_EMPTY_ERROR_MESSAGE)
                                .build());
        By elementSelector = searchResult.getSelector();

        if (elementSelector == NOT_EMPTY_NEMID_TOKEN) {
            return searchResult.getElementTextTrimmed();
        }
        if (elementSelector == NOT_EMPTY_ERROR_MESSAGE) {
            throwCodeTokenErrorMessage(searchResult.getElementTextTrimmed());
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't find NemId token for code token authentication");
    }

    private void throwCodeTokenErrorMessage(String errorMessage) {
        String errorMessageLowerCase = errorMessage.toLowerCase();

        if (NemIdUtils.valueMatchesAnyPattern(
                errorMessageLowerCase, INVALID_CARD_OR_TOKEN_CODE_PATTERNS)) {
            throw NemIdError.INVALID_CODE_TOKEN_CODE.exception();
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Unknown code token error message: " + errorMessage);
    }
}

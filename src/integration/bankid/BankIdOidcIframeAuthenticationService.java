package src.integration.bankid;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import src.integration.bankid.BankIdOidcConstants.QueryParamKeys;
import src.integration.bankid.BankIdOidcConstants.Urls;
import src.integration.bankid.rpc.BankIdJsResponse;

@RequiredArgsConstructor
public class BankIdOidcIframeAuthenticationService {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String callbackUrl;
    private final boolean isInTestContext;

    public String displayIframeAndWaitForAuthorizationCode(String iFrameUrl) {

        URL urlForPageThatWillDisplayIframe =
                new URL(Urls.getBankIdIframePage(isInTestContext))
                        .queryParam(QueryParamKeys.STATE, strongAuthenticationState.getState())
                        .queryParam(QueryParamKeys.IFRAME_URL, iFrameUrl)
                        .queryParam(QueryParamKeys.CALLBACK_URL, callbackUrl);

        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(urlForPageThatWillDisplayIframe));

        BankIdJsResponse bankIdJsResponse =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(), 2, TimeUnit.MINUTES)
                        .map(BankIdJsResponse::fromQueryParametersMap)
                        .orElseThrow(BankIdError.TIMEOUT::exception);

        if (bankIdJsResponse.hasError()) {
            bankIdJsResponse.throwBankIdError();
        }

        return bankIdJsResponse.getCode();
    }
}

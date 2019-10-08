package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CbiGlobeAuthenticationRedirectController extends CbiGlobeAuthenticationController {

    public CbiGlobeAuthenticationRedirectController(
            SupplementalInformationHelper supplementalInformationHelper,
            CbiGlobeAuthenticator authenticator,
            StrongAuthenticationState consentState) {
        super(supplementalInformationHelper, authenticator, consentState);
    }

    public void accountConsentAuthentication() {
        String redirectUrl =
                authenticator.createRedirectUrl(consentState.getState(), ConsentType.ACCOUNT);
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(
                        redirectUrl, this.authenticator.createConsentRequestAccount());

        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
        waitForSuplementalInformation(ConsentType.ACCOUNT);
    }

    public void transactionsConsentAuthentication(GetAccountsResponse getAccountsResponse) {
        String redirectUrl =
                authenticator.createRedirectUrl(
                        consentState.getState(), ConsentType.BALANCE_TRANSACTION);
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(
                        redirectUrl,
                        this.authenticator.createConsentRequestBalancesTransactions(
                                getAccountsResponse));

        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));
        waitForSuplementalInformation(ConsentType.BALANCE_TRANSACTION);
    }
}

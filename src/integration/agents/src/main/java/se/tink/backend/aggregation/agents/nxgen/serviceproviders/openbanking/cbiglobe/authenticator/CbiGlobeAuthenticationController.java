package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class CbiGlobeAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final CbiGlobeAuthenticator authenticator;
    private final StrongAuthenticationState consentState;

    public CbiGlobeAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            CbiGlobeAuthenticator authenticator,
            StrongAuthenticationState consentState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.consentState = consentState;
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        authenticator.autoAutenthicate();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        // Consent for accounts
        waitForSuplementalInformation(consentState);

        // account fetching in AUTHENTICATING phase due to two times consent authentication flow
        GetAccountsResponse getAccountsResponse = authenticator.fetchAccounts();
        openThirdPartyApp(getAccountsResponse);

        // Consent for transactions and balances
        waitForSuplementalInformation(consentState);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        this.authenticator.tokenAutoAuthentication();
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(
                        consentState.getState(), this.authenticator.createConsentRequestAccount());
        return getAppPayload(authorizeUrl);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    public void openThirdPartyApp(GetAccountsResponse getAccountsResponse) {
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(
                        this.consentState.getState(),
                        this.authenticator.createConsentRequestBalancesTransactions(
                                getAccountsResponse));
        ThirdPartyAppAuthenticationPayload payload = this.getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private void waitForSuplementalInformation(StrongAuthenticationState state) {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                state.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
    }
}

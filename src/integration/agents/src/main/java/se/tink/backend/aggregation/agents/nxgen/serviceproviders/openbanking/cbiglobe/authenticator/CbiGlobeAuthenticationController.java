package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public abstract class CbiGlobeAuthenticationController
        implements AutoAuthenticator, MultiFactorAuthenticator {
    protected final SupplementalInformationHelper supplementalInformationHelper;
    protected final CbiGlobeAuthenticator authenticator;
    protected final StrongAuthenticationState consentState;

    public CbiGlobeAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            CbiGlobeAuthenticator authenticator,
            StrongAuthenticationState consentState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.consentState = consentState;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        authenticator.autoAutenthicate();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        this.authenticator.tokenAutoAuthentication();
        // CBI Globe flow needs two authentcation for AIS: 1. accounts 2. balances and transactions

        accountConsentAuthentication();

        // Fetching accounts authentication phase due to double consent authentication
        GetAccountsResponse getAccountsResponse = authenticator.fetchAccounts();

        transactionsConsentAuthentication(getAccountsResponse);
    }

    protected abstract void accountConsentAuthentication();

    protected abstract void transactionsConsentAuthentication(
            GetAccountsResponse getAccountsResponse);

    protected void waitForSuplementalInformation(ConsentType consentType) {
        Optional<Map<String, String>> queryMap =
                this.supplementalInformationHelper.waitForSupplementalInformation(
                        this.consentState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        Optional<String> codeValue =
                Optional.ofNullable(
                        Optional.ofNullable(queryMap.get().get(QueryKeys.CODE))
                                .orElse(consentType.getCode()));

        if (!codeValue.get().equalsIgnoreCase(consentType.getCode())) {
            waitForSuplementalInformation(consentType);
        }
    }
}

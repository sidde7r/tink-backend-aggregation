package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public abstract class CbiGlobeAuthenticationController
        implements AutoAuthenticator, TypedAuthenticator {
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
        authenticator.autoAuthenticate();
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

    protected abstract void accountConsentAuthentication() throws AuthenticationException;

    protected abstract void transactionsConsentAuthentication(
            GetAccountsResponse getAccountsResponse) throws AuthenticationException;

    protected void waitForSupplementalInformation(ConsentType consentType) throws LoginException {
        Optional<Map<String, String>> queryMap =
                this.supplementalInformationHelper.waitForSupplementalInformation(
                        this.consentState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);

        String codeValue =
                queryMap.orElseThrow(
                                () -> new LoginException(LoginError.CREDENTIALS_VERIFICATION_ERROR))
                        .getOrDefault(QueryKeys.CODE, consentType.getCode());

        if (!codeValue.equalsIgnoreCase(consentType.getCode())) {
            waitForSupplementalInformation(consentType);
        }
    }
}

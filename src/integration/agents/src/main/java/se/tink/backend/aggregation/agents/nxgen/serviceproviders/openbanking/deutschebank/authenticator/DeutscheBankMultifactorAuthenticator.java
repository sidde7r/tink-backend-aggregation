package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankMultifactorAuthenticator implements TypedAuthenticator, AutoAuthenticator {

    private final DeutscheBankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String iban;
    private final String psuId;
    private final StrongAuthenticationState strongAuthenticationState;
    private final DeutscheBankRedirectHandler deutscheBankRedirectHandler;

    public DeutscheBankMultifactorAuthenticator(
            DeutscheBankApiClient apiClient,
            PersistentStorage persistentStorage,
            String iban,
            String psuId,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.iban = iban;
        this.psuId = psuId;
        this.strongAuthenticationState = strongAuthenticationState;
        this.deutscheBankRedirectHandler =
                new DeutscheBankRedirectHandler(supplementalInformationHelper);
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        ConsentBaseResponse consent =
                apiClient.getConsent(strongAuthenticationState.getState(), iban, psuId);
        persistentStorage.put(DeutscheBankConstants.StorageKeys.CONSENT_ID, consent.getConsentId());
        deutscheBankRedirectHandler.handleRedirect();
        poll();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    private void poll() throws ThirdPartyAppException {
        for (int i = 0; i < DeutscheBankConstants.FormValues.MAX_POLLS_COUNTER; i++) {
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            ConsentStatusResponse consentStatusResponse = apiClient.getConsentStatus();
            String consentStatus = consentStatusResponse.getConsentStatus();
            switch (consentStatus) {
                case DeutscheBankConstants.StatusValues.VALID:
                    return;
                case DeutscheBankConstants.StatusValues.RECEIVED:
                    continue;
                case DeutscheBankConstants.StatusValues.EXPIRED:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    break;
            }
        }
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}

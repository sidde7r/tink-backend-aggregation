package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class PostbankAuthenticator implements AutoAuthenticator {

    private final PostbankApiClient postbankApiClient;
    private final PersistentStorage persistentStorage;
    private final String iban;

    public PostbankAuthenticator(
            PostbankApiClient apiClient, PersistentStorage persistentStorage, String iban) {
        this.postbankApiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.iban = iban;
    }

    public AuthorisationResponse init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        ConsentResponse consentsResponse = postbankApiClient.getConsents(iban, username);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentsResponse.getConsentId());

        return postbankApiClient.startAuthorisation(
                new URL(
                        consentsResponse
                                .getLinksEntity()
                                .getStartAuthorisationWithEncryptedPsuAuthenticationEntity()
                                .getHref()),
                username,
                password);
    }

    AuthorisationResponse selectScaMethod(String methodId, String username, String url) {
        return postbankApiClient.updateAuthorisationForScaMethod(new URL(url), username, methodId);
    }

    AuthorisationResponse authenticateWithOtp(String otp, String username, String url)
            throws AuthenticationException, AuthorizationException {
        return postbankApiClient.updateAuthorisationForOtp(new URL(url), username, otp);
    }

    AuthorisationResponse checkStatus(String username, String url) {
        return postbankApiClient.getAuthorisation(new URL(url), username);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}

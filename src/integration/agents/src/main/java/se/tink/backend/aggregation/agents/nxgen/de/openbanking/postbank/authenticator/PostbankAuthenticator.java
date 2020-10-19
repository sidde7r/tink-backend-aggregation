package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
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

    public PostbankAuthenticator(PostbankApiClient apiClient, PersistentStorage persistentStorage) {
        this.postbankApiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    public AuthorisationResponse init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        ConsentResponse consentsResponse = postbankApiClient.getConsents(username);
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

    AuthorisationResponse authoriseWithOtp(String otp, String username, String url)
            throws AuthenticationException, AuthorizationException {
        return postbankApiClient.updateAuthorisationForOtp(new URL(url), username, otp);
    }

    AuthorisationResponse checkAuthorisationStatus(String username, String url) {
        return postbankApiClient.getAuthorisation(new URL(url), username);
    }

    @Override
    public void autoAuthenticate() {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}

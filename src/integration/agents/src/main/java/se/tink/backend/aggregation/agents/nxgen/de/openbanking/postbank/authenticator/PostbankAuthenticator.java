package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@AllArgsConstructor
public class PostbankAuthenticator implements AutoAuthenticator {

    private final PostbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    AuthorisationResponse init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        ConsentResponse consentsResponse = apiClient.getConsents(username);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentsResponse.getConsentId());
        String startAuthorisationLink =
                consentsResponse
                        .getLinks()
                        .getStartAuthorisationWithEncryptedPsuAuthentication()
                        .getHref();

        return startAuthorsation(startAuthorisationLink, username, password);
    }

    AuthorisationResponse startAuthorsation(String scaLink, String username, String password) {
        return apiClient.startAuthorisation(new URL(scaLink), username, password);
    }

    AuthorisationResponse selectScaMethod(String methodId, String username, String url) {
        return apiClient.updateAuthorisationForScaMethod(new URL(url), username, methodId);
    }

    AuthorisationResponse authoriseWithOtp(String otp, String username, String url)
            throws AuthenticationException, AuthorizationException {
        return apiClient.updateAuthorisationForOtp(new URL(url), username, otp);
    }

    AuthorisationResponse checkAuthorisationStatus(String username, String url) {
        return apiClient.getAuthorisation(new URL(url), username);
    }

    @Override
    public void autoAuthenticate() {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails();
        if (!consentDetailsResponse.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    public void validateAndStoreConsentDetails() {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails();
        if (!consentDetailsResponse.isValid()) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }
}

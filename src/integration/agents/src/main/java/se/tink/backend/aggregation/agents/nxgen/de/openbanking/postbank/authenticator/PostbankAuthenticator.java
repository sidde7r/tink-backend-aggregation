package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.PostbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class PostbankAuthenticator implements AutoAuthenticator {

    private PostbankApiClient postbankApiClient;
    private final SessionStorage sessionStorage;
    private final String iban;

    public PostbankAuthenticator(
            DeutscheBankApiClient apiClient, SessionStorage sessionStorage, String iban) {
        this.postbankApiClient = (PostbankApiClient) apiClient;
        this.sessionStorage = sessionStorage;
        this.iban = iban;
    }

    public AuthorisationResponse init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        ConsentResponse consentsResponse = postbankApiClient.getConsents(iban, username);
        sessionStorage.put(StorageKeys.CONSENT_ID, consentsResponse.getConsentId());

        AuthorisationResponse authorisationResponse =
                postbankApiClient.startAuthorisation(
                        new URL(
                                consentsResponse
                                        .getLinksEntity()
                                        .getStartAuthorisationWithEncryptedPsuAuthenticationEntity()
                                        .getHref()),
                        username,
                        password);

        return authorisationResponse;
    }

    public AuthorisationResponse selectScaMethod(String methodId, String username, String url) {
        return postbankApiClient.updateAuthorisationForScaMethod(new URL(url), username, methodId);
    }

    public AuthorisationResponse authenticateWithOtp(String otp, String username, String url)
            throws AuthenticationException, AuthorizationException {
        return postbankApiClient.updateAuthorisationForOtp(new URL(url), username, otp);
    }

    public AuthorisationResponse checkStatus(String username, String url) {
        return postbankApiClient.getAuthorisation(new URL(url), username);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}

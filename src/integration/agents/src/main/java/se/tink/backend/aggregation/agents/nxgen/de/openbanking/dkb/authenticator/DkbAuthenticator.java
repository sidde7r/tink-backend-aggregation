package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.MAX_CONSENT_VALIDITY_DAYS;

import java.time.LocalDate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

@Slf4j
public class DkbAuthenticator implements PasswordAuthenticator {

    private final DkbAuthApiClient authApiClient;
    private final DkbSupplementalDataProvider supplementalDataProvider;
    private final DkbStorage storage;

    public DkbAuthenticator(
            DkbAuthApiClient authApiClient,
            DkbSupplementalDataProvider supplementalDataProvider,
            DkbStorage storage) {
        this.authApiClient = authApiClient;
        this.supplementalDataProvider = supplementalDataProvider;
        this.storage = storage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        getWso2Token();
        authenticateUser(username, password);
        getConsent();
    }

    private void getWso2Token() throws LoginException {
        Wso2Token token = authApiClient.getWso2Token();
        storage.setWso2OAuthToken(token.toOAuth2Token());
    }

    private void authenticateUser(String username, String password) throws AuthenticationException {
        AuthResult result = authenticate1stFactor(username, password);
        result = authenticate2ndFactor(result);
        processAuthenticationResult(result);
    }

    private void processAuthenticationResult(AuthResult result) throws LoginException {
        if (!result.isAuthenticated()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        storage.setAccessToken(result.toOAuth2Token());
    }

    private AuthResult authenticate1stFactor(String username, String password)
            throws LoginException {
        return authApiClient.authenticate1stFactor(username, password);
    }

    private AuthResult authenticate2ndFactor(AuthResult previousResult)
            throws AuthenticationException {
        if (previousResult.isAuthenticationFinished()) {
            return previousResult;
        }
        log.info(
                "Authentication process is not finished. Authentication result returnCode [{}] and actionCode [{}]",
                previousResult.getReturnCode(),
                previousResult.getActionCode());
        AuthResult result = select2ndFactorMethodIfNeeded(previousResult);
        return provide2ndFactorCode(result);
    }

    private AuthResult select2ndFactorMethodIfNeeded(AuthResult previousResult)
            throws AuthenticationException {
        if (!previousResult.isAuthMethodSelectionRequired()) {
            log.info("Authentication method selection is not required");
            return previousResult;
        }
        log.info("Selection of authentication method is required");

        String methodId =
                supplementalDataProvider.selectAuthMethod(
                        previousResult.getSelectableAuthMethods());
        return authApiClient.select2ndFactorAuthMethod(methodId);
    }

    private AuthResult provide2ndFactorCode(AuthResult previousResult)
            throws AuthenticationException {
        if (previousResult.isAuthenticationFinished()) {
            log.info(
                    "Authentication process is finished. Authentication result returnCode [{}] and actionCode [{}]",
                    previousResult.getReturnCode(),
                    previousResult.getActionCode());
            return previousResult;
        }
        log.info("Authentication is not finished. Need to provide TAN code");
        return authApiClient.submit2ndFactorTanCode(getTanByAuthTypeSelected(previousResult));
    }

    private String getTanByAuthTypeSelected(AuthResult previousResult)
            throws SupplementalInfoException {
        if (previousResult.getChallenge().contains("####")) {
            return previousResult
                    .getChallenge()
                    .substring(0, previousResult.getChallenge().indexOf("#"));
        }
        log.info(
                "AuthTypeSelected is [{}], so TAN needs to be provided from external resource",
                previousResult.getAuthTypeSelected());
        return supplementalDataProvider.getTanCode();
    }

    private void getConsent() throws AuthenticationException {
        Consent consent = getExistingConsent().orElseGet(this::createNewConsent);
        if (consent.isNotAuthorized()) {
            authorizeConsent(consent.getConsentId());
        }
        storage.setConsentId(consent.getConsentId());
    }

    private Optional<Consent> getExistingConsent() {
        return storage.getConsentId().map(authApiClient::getConsent).filter(Consent::isValid);
    }

    private Consent createNewConsent() {
        return authApiClient.createConsent(getMaxConsentValidityDate());
    }

    private LocalDate getMaxConsentValidityDate() {
        return LocalDate.now().plusDays(MAX_CONSENT_VALIDITY_DAYS);
    }

    private void authorizeConsent(String consentId) throws AuthenticationException {
        ConsentAuthorization consentAuth = startConsentAuthorization(consentId);
        selectConsentAuthorizationMethodIfNeeded(
                consentId, consentAuth.getAuthorisationId(), consentAuth);
        provide2ndFactorConsentAuthorization(consentId, consentAuth.getAuthorisationId());
    }

    private ConsentAuthorization startConsentAuthorization(String consentId) throws LoginException {
        return authApiClient.startConsentAuthorization(consentId);
    }

    private ConsentAuthorization selectConsentAuthorizationMethodIfNeeded(
            String consentId, String authorizationId, ConsentAuthorization previousResult)
            throws AuthenticationException {
        if (!previousResult.isScaMethodSelectionRequired()) {
            return previousResult;
        }

        String methodId = supplementalDataProvider.selectAuthMethod(previousResult.getScaMethods());
        return authApiClient.selectConsentAuthorizationMethod(consentId, authorizationId, methodId);
    }

    private void provide2ndFactorConsentAuthorization(String consentId, String authorizationId)
            throws SupplementalInfoException, LoginException {
        String code = supplementalDataProvider.getTanCode();
        authApiClient.consentAuthorization2ndFactor(consentId, authorizationId, code);
    }
}

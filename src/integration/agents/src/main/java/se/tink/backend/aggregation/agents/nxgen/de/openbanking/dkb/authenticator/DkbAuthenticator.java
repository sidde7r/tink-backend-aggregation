package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.MAX_CONSENT_VALIDITY_DAYS;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class DkbAuthenticator implements AutoAuthenticator, MultiFactorAuthenticator {

    private static final String STATIC_SALT = "diH6uxoh5gie)b0she=n";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private final DkbAuthApiClient authApiClient;
    private final DkbSupplementalDataProvider supplementalDataProvider;
    private final DkbStorage storage;
    private final Credentials credentials;

    public DkbAuthenticator(
            DkbAuthApiClient authApiClient,
            DkbSupplementalDataProvider supplementalDataProvider,
            DkbStorage storage,
            Credentials credentials) {
        this.authApiClient = authApiClient;
        this.supplementalDataProvider = supplementalDataProvider;
        this.storage = storage;
        this.credentials = credentials;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        logHashes(credentials);

        getWso2Token();
        authenticateUser(username, password);
        createConsentAndAuthorize();
    }

    private void logHashes(Credentials credentials) {
        // There are a lot of invalid_credentials thrown.
        // Users often finally manages to provide correct credentials in 2nd or 3rd attempt.
        // We want to investigate if users have problems with providing username or password.
        // To achieve that - this logging will be helpful. We will check the hashes from
        // unsuccessful and successful authentications for the same credentialsId / userId and check
        // whether username hash or credentials hash changed.
        log.info(
                "[DKB Auth] Hashes: {}, {}",
                ENCODER.encodeToString(
                                Hash.sha512(credentials.getField(Field.Key.USERNAME) + STATIC_SALT))
                        .substring(0, 6),
                ENCODER.encodeToString(
                                Hash.sha512(credentials.getField(Field.Key.PASSWORD) + STATIC_SALT))
                        .substring(0, 6));
    }

    @Override
    public void autoAuthenticate() {
        if (!storage.getConsentId().isPresent()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        // Check validity of wsoToken, one that allows developer portal application to use APIs
        // If not valid, get a new one
        if (!storage.getWso2OAuthToken().isValid()) {
            getWso2Token();
        }

        Optional<ConsentDetailsResponse> maybeConsent = Optional.empty();
        // Check two things at once. It can throw in case of oauth token expiry (token for
        // retrieving end user data). It doesn't come with expiresAt, so we need to check it by
        // doing.
        try {
            maybeConsent = getExistingConsent();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401
                    && e.getResponse().hasBody()
                    && e.getResponse().getBody(String.class).contains("TOKEN_EXPIRED")) {
                getNewAccessTokenForAutoAuthentication();
                maybeConsent = getExistingConsent();
            }
        }

        ConsentDetailsResponse consent =
                maybeConsent.orElseThrow(SessionError.SESSION_EXPIRED::exception);
        if (consent.isExpired()) {
            throw SessionError.CONSENT_EXPIRED.exception();
        }
        if (consent.isRevokedByPsu()) {
            throw SessionError.CONSENT_REVOKED_BY_USER.exception();
        }
        if (!consent.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        credentials.setSessionExpiryDate(consent.getValidUntil());
    }

    private void getNewAccessTokenForAutoAuthentication() {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        // We try to get a new access token by authenticating with credentials only.
        // This should not trigger second factor.
        AuthResult authResult = authenticate1stFactor(username, password);
        // Give up on autoAuthentication if process not finished, ie. requiring 2nd factor.
        if (!authResult.isAuthenticated()) {
            throw SessionError.SESSION_EXPIRED.exception(
                    "Failed to gather new oauth token during auto authentication.");
        } else {
            // Save new accessToken so it will be used for subsequent calls.
            storage.setAccessToken(authResult.toOAuth2Token());
        }
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

    private AuthResult authenticate1stFactor(String username, String password) {
        try {
            return authApiClient.authenticate1stFactor(username, password);
        } catch (HttpResponseException httpResponseException) {
            HttpResponse httpResponse = httpResponseException.getResponse();
            if (httpResponse.getStatus() == 400 && !httpResponse.hasBody()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            } else {
                throw httpResponseException;
            }
        }
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
        return supplementalDataProvider.getTanCode(
                Collections.singletonList(previousResult.getChallenge()));
    }

    private void createConsentAndAuthorize() throws AuthenticationException {
        ConsentResponse consentResponse = createNewConsent();
        if (consentResponse.isNotAuthorized()) {
            authorizeConsent(consentResponse.getConsentId());
        }
        ConsentDetailsResponse consentDetailsResponse =
                authApiClient.getConsentDetails(consentResponse.getConsentId());
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    private Optional<ConsentDetailsResponse> getExistingConsent() {
        return storage.getConsentId().map(authApiClient::getConsentDetails);
    }

    private ConsentResponse createNewConsent() {
        LocalDate maxConsentValidityDate = LocalDate.now().plusDays(MAX_CONSENT_VALIDITY_DAYS);
        ConsentResponse consent = authApiClient.createConsent(maxConsentValidityDate);
        storage.setConsentId(consent.getConsentId());
        return consent;
    }

    private void authorizeConsent(String consentId) throws AuthenticationException {
        ConsentAuthorization consentAuth = authApiClient.startConsentAuthorization(consentId);
        ConsentAuthorization consentAuthWithSelectedMethod =
                selectConsentAuthorizationMethodIfNeeded(consentId, consentAuth);
        consentAuthWithSelectedMethod.checkIfChallengeDataIsAllowed();
        provide2ndFactorConsentAuthorization(
                consentId, consentAuth.getAuthorisationId(), consentAuthWithSelectedMethod);
    }

    private ConsentAuthorization selectConsentAuthorizationMethodIfNeeded(
            String consentId, ConsentAuthorization previousResult) throws AuthenticationException {
        if (!previousResult.isScaMethodSelectionRequired()) {
            return previousResult;
        }

        String methodId =
                supplementalDataProvider.selectAuthMethod(previousResult.getAllowedScaMethods());
        return authApiClient.selectConsentAuthorizationMethod(
                consentId, previousResult.getAuthorisationId(), methodId);
    }

    private void provide2ndFactorConsentAuthorization(
            String consentId, String authorisationId, ConsentAuthorization consentAuth)
            throws SupplementalInfoException, LoginException {
        String code =
                supplementalDataProvider.getTanCode(
                        consentAuth.getChosenScaMethod(),
                        consentAuth.getChallengeData().getData(),
                        consentAuth.getChallengeData());
        authApiClient.consentAuthorization2ndFactor(consentId, authorisationId, code);
    }
}

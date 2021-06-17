package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.MAX_CONSENT_VALIDITY_DAYS;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class DkbAuthenticator implements AutoAuthenticator, MultiFactorAuthenticator {

    private final DkbAuthApiClient authApiClient;
    private final DkbSupplementalDataProvider supplementalDataProvider;
    private final DkbStorage storage;
    private final Credentials credentials;

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

        ensureEidasCertRegisteredInDkb();

        authenticateUser(username, password);
        createConsentAndAuthorize();
    }

    @Override
    public void autoAuthenticate() {
        if (!storage.getConsentId().isPresent()) {
            throw SessionError.SESSION_EXPIRED.exception();
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

    private void authenticateUser(String username, String password) throws AuthenticationException {
        AuthResult result = authenticate1stFactor(username, password);
        result = authenticate2ndFactor(result);
        processAuthenticationResult(result);
    }

    private void processAuthenticationResult(AuthResult result) throws LoginException {
        if (!result.isAuthenticated()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        log.info("[DKB Auth] User successfully authenticated {}", result.getAuthTypeSelected());
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
            log.info("[DKB Auth] User did not have to do 2FA in authentication");
            return previousResult;
        }
        log.info(
                "[DKB Auth] Authentication process is not finished. Authentication result returnCode [{}] and actionCode [{}]",
                previousResult.getReturnCode(),
                previousResult.getActionCode());
        AuthResult result = select2ndFactorMethodIfNeeded(previousResult);
        return provide2ndFactorCode(result);
    }

    private AuthResult select2ndFactorMethodIfNeeded(AuthResult previousResult)
            throws AuthenticationException {
        if (!previousResult.isAuthMethodSelectionRequired()) {
            log.info(
                    "[DKB Auth] Authentication method selection is not required in authentication.");
            log.info(
                    "[DKB Auth] User for authenticationType {} started 2FA in authentication.",
                    previousResult.getAuthTypeSelected());
            return previousResult;
        }
        log.info("[DKB Auth] Selection of authentication method is required in authentication.");

        SelectableMethod selectedAuthMethod =
                supplementalDataProvider.selectAuthMethod(
                        previousResult.getSelectableAuthMethods());
        log.info(
                "[DKB Auth] User for authenticationType {} started 2FA in authentication.",
                selectedAuthMethod.getAuthenticationType());
        return authApiClient.select2ndFactorAuthMethod(selectedAuthMethod.getIdentifier());
    }

    private AuthResult provide2ndFactorCode(AuthResult previousResult)
            throws AuthenticationException {
        if (previousResult.isAuthenticationFinished()) {
            log.info(
                    "[DKB Auth] Authentication process is finished. Authentication result returnCode [{}] and actionCode [{}]",
                    previousResult.getReturnCode(),
                    previousResult.getActionCode());
            return previousResult;
        }
        log.info("[DKB Auth] Authentication is not finished. Need to provide TAN code");
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
                "[DKB Auth] AuthTypeSelected is [{}], so TAN needs to be provided from external resource",
                previousResult.getAuthTypeSelected());
        return supplementalDataProvider.getTanCode(
                Collections.singletonList(previousResult.getChallenge()));
    }

    private void createConsentAndAuthorize() throws AuthenticationException {
        ConsentResponse consentResponse = createNewConsent();
        if (consentResponse.isNotAuthorized()) {
            log.info("[DKB Auth] Consent is not authorized, trying to authorize");
            authorizeConsent(consentResponse.getConsentId());
        }
        ConsentDetailsResponse consentDetailsResponse =
                authApiClient.getConsentDetails(consentResponse.getConsentId());
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    private Optional<ConsentDetailsResponse> getExistingConsent() {
        return storage.getConsentId().map(authApiClient::getConsentDetails);
    }

    private void ensureEidasCertRegisteredInDkb() {
        try {
            authApiClient.createConsent(LocalDate.now());
        } catch (HttpResponseException e) {
            // This is expected to throw an error of expected body.
            if (!e.getResponse().hasBody()
                    || !(e.getResponse().getBody(String.class) != null
                            && e.getResponse().getBody(String.class).contains("TOKEN_UNKNOWN"))) {
                log.warn("[DKB Auth] DKB returned unexpected error in preregister method!", e);
            }
        }
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
        log.info(
                "[DKB Auth] User for authentication {} successfully passed 2FA to authorize consent.",
                ObjectUtils.firstNonNull(
                        consentAuthWithSelectedMethod.getChosenScaMethod().getAuthenticationType(),
                        consentAuthWithSelectedMethod.getChosenScaMethod().getName()));
    }

    private ConsentAuthorization selectConsentAuthorizationMethodIfNeeded(
            String consentId, ConsentAuthorization previousResult) throws AuthenticationException {
        if (!previousResult.isScaMethodSelectionRequired()) {
            log.info(
                    "[DKB Auth] Sca Method selection not required, authenticationType is not available.");
            log.info(
                    "[DKB Auth] User for authenticationType is hard to determine, using name {} to authorize consent.",
                    previousResult.getChosenScaMethod().getName());
            return previousResult;
        }

        List<ConsentAuthorization.ScaMethod> allowedScaMethods =
                previousResult.getAllowedScaMethods();
        SelectableMethod selectedAuthMethod =
                supplementalDataProvider.selectAuthMethod(allowedScaMethods);
        log.info(
                "[DKB Auth] User for authenticationType {} started 2FA to authorize consent.",
                selectedAuthMethod.getAuthenticationType());
        ConsentAuthorization consentAuthorization =
                authApiClient.selectConsentAuthorizationMethod(
                        consentId,
                        previousResult.getAuthorisationId(),
                        selectedAuthMethod.getIdentifier());
        setMissingAuthenticationType(selectedAuthMethod, consentAuthorization);
        return consentAuthorization;
    }

    private void setMissingAuthenticationType(
            SelectableMethod selectedAuthMethod, ConsentAuthorization consentAuthorization) {
        consentAuthorization
                .getChosenScaMethod()
                .setAuthenticationType(selectedAuthMethod.getAuthenticationType());
    }

    private void provide2ndFactorConsentAuthorization(
            String consentId, String authorisationId, ConsentAuthorization consentAuth)
            throws SupplementalInfoException, LoginException {
        String code =
                supplementalDataProvider.getTanCode(
                        consentAuth.getChosenScaMethod(), consentAuth.getChallengeData().getData());
        authApiClient.consentAuthorization2ndFactor(consentId, authorisationId, code);
    }
}

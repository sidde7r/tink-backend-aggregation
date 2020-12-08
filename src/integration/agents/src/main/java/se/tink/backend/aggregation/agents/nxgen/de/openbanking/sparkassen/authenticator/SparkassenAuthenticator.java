package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.AuthMethods;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.FieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.Catalog;

@Slf4j
public class SparkassenAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {
    private static final String VALID = "valid";
    private static final String FINALISED = "finalised";
    private static final String FAILED = "failed";

    private static final String PSU_AUTHENTICATED = "psuAuthenticated";
    private static final String SCA_METHOD_SELECTED = "scaMethodSelected";

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SparkassenApiClient apiClient;
    private final SparkassenPersistentStorage persistentStorage;
    private final FieldBuilder fieldBuilder;

    public SparkassenAuthenticator(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            SparkassenApiClient apiClient,
            SparkassenPersistentStorage persistentStorage) {
        this.supplementalInformationHelper =
                Preconditions.checkNotNull(supplementalInformationHelper);
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.fieldBuilder = new FieldBuilder(Preconditions.checkNotNull(catalog));
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        String consentId = persistentStorage.getConsentId();
        try {
            if (Strings.isNullOrEmpty(consentId) || !isConsentValid(consentId)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            log.info("Caught exception during autoAuth, finishing with TEMP_ERROR", e);
            if (e.getResponse().getStatus() == 503) {
                throw BankServiceError.NO_BANK_SERVICE.exception(e);
            } else {
                throw e;
            }
        }
    }

    private boolean isConsentValid(String consentId) {
        return VALID.equalsIgnoreCase(apiClient.getConsentStatus(consentId).getConsentStatus());
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        validateInput(credentials);

        ConsentResponse consentResponse = initializeProcess();

        AuthenticationMethodResponse initAuthorizationResponse =
                initializeAuthorizationOfConsent(
                        consentResponse,
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        AuthenticationMethodResponse scaMethodDetails =
                getScaMethodDetails(initAuthorizationResponse);

        authorizeConsentWithOtp(
                scaMethodDetails.getChosenScaMethod(), scaMethodDetails.getChallengeData());
    }

    private void validateInput(Credentials credentials) throws LoginException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), this.getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        validateCredentialPresent(credentials, Field.Key.USERNAME);
        validateCredentialPresent(credentials, Field.Key.PASSWORD);
    }

    private void validateCredentialPresent(Credentials credentials, Field.Key key)
            throws LoginException {
        if (Strings.isNullOrEmpty(credentials.getField(key))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private ConsentResponse initializeProcess() throws LoginException {
        ConsentResponse consentResponse = apiClient.createConsent();
        persistentStorage.saveConsentId(consentResponse.getConsentId());
        return consentResponse;
    }

    private AuthenticationMethodResponse initializeAuthorizationOfConsent(
            ConsentResponse consentResponse, String username, String password)
            throws AuthenticationException {

        URL url =
                Optional.ofNullable(consentResponse.getLinks())
                        .map(LinksEntity::getStartAuthorisationWithPsuAuthentication)
                        .map(Href::getHref)
                        .map(URL::new)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_SCA_AUTHORIZATION_URL));

        AuthenticationMethodResponse initAuthorizationResponse =
                apiClient.initializeAuthorization(url, username, password);
        persistentStorage.saveAuthorizationId(initAuthorizationResponse.getAuthorisationId());
        return initAuthorizationResponse;
    }

    private AuthenticationMethodResponse getScaMethodDetails(
            AuthenticationMethodResponse initAuthorizationResponse)
            throws SupplementalInfoException, LoginException {
        switch (initAuthorizationResponse.getScaStatus()) {
            case PSU_AUTHENTICATED:
                return getScaMethodDetailsOutOfMultiplePossible(
                        getSupportedScaMethods(initAuthorizationResponse));
            case SCA_METHOD_SELECTED:
                return initAuthorizationResponse;
            default:
                throw new IllegalStateException(ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        }
    }

    private List<ScaMethodEntity> getSupportedScaMethods(
            AuthenticationMethodResponse initAuthResponse) throws LoginException {

        List<ScaMethodEntity> methods =
                initAuthResponse.getScaMethods().stream()
                        .filter(
                                scaMethod ->
                                        !AuthMethods.UNSUPPORTED_AUTH_TYPES.contains(
                                                scaMethod.getAuthenticationMethodId()))
                        .collect(Collectors.toList());

        if (methods.isEmpty()) {
            throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.STARTCODE_NOT_FOUND);
        }
        return methods;
    }

    private AuthenticationMethodResponse getScaMethodDetailsOutOfMultiplePossible(
            List<ScaMethodEntity> scaMethods) throws SupplementalInfoException {
        ScaMethodEntity chosenScaMethod = collectScaMethod(scaMethods);
        AuthenticationMethodResponse authenticationMethodResponse =
                apiClient.selectAuthorizationMethod(
                        persistentStorage.getConsentId(),
                        persistentStorage.getAuthorizationId(),
                        chosenScaMethod.getAuthenticationMethodId());

        if (authenticationMethodResponse.getChallengeData() == null) {
            throw new IllegalStateException(ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        } else {
            return authenticationMethodResponse;
        }
    }

    private ScaMethodEntity collectScaMethod(List<ScaMethodEntity> scaMethods)
            throws SupplementalInfoException {
        if (scaMethods.size() == 1) {
            return scaMethods.get(0);
        }

        Field scaMethodField = fieldBuilder.getChooseScaMethodField(scaMethods);
        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(scaMethodField);
        int selectedIndex =
                Integer.parseInt(supplementalInformation.get(scaMethodField.getName())) - 1;

        return scaMethods.get(selectedIndex);
    }

    private void authorizeConsentWithOtp(
            ScaMethodEntity scaMethod, ChallengeDataEntity challengeData)
            throws AuthenticationException {
        String otp = collectOtp(scaMethod, challengeData);
        FinalizeAuthorizationResponse finalizeAuthorizationResponse =
                apiClient.finalizeAuthorization(
                        persistentStorage.getConsentId(),
                        persistentStorage.getAuthorizationId(),
                        otp);

        switch (finalizeAuthorizationResponse.getScaStatus()) {
            case FINALISED:
                break;
            case FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    private String collectOtp(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = fieldBuilder.getOtpFields(scaMethod, challengeData);
        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(
                        fields.toArray(new Field[0]));
        return supplementalInformation.get(fields.get(fields.size() - 1).getName());
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.FieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.Catalog;

public class SparkassenAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {
    private static final String VALID = "valid";
    private static final String FINALISED = "finalised";
    private static final String FAILED = "failed";

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

        if (Strings.isNullOrEmpty(consentId) || !isConsentValid(consentId)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private boolean isConsentValid(String consentId) {
        return VALID.equalsIgnoreCase(apiClient.getConsentStatus(consentId).getConsentStatus());
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        validateInput(credentials);

        ConsentResponse consentResponse = initializeProcess(getIbansList(credentials));

        InitAuthorizationResponse initAuthorizationResponse =
                initializeAuthorizationOfConsent(
                        consentResponse,
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        Pair<ScaMethodEntity, ChallengeDataEntity> pair =
                getScaMethodDetails(initAuthorizationResponse);

        authorizeConsentWithOtp(
                pair.getLeft().getAuthenticationType(), pair.getRight().getOtpMaxLength());
    }

    private void validateInput(Credentials credentials) throws LoginException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), this.getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        validateCredentialPresent(credentials, Field.Key.USERNAME);
        validateCredentialPresent(credentials, Field.Key.PASSWORD);
        validateCredentialPresent(credentials, Field.Key.IBAN);
    }

    private void validateCredentialPresent(Credentials credentials, Field.Key key)
            throws LoginException {
        if (Strings.isNullOrEmpty(credentials.getField(key))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private List<String> getIbansList(Credentials credentials) {
        return Arrays.asList(credentials.getField(Field.Key.IBAN).split(","));
    }

    private ConsentResponse initializeProcess(List<String> ibans) {
        ConsentResponse consentResponse = apiClient.createConsent(ibans);
        persistentStorage.saveConsentId(consentResponse.getConsentId());
        return consentResponse;
    }

    private InitAuthorizationResponse initializeAuthorizationOfConsent(
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

        InitAuthorizationResponse initAuthorizationResponse =
                apiClient.initializeAuthorization(url, username, password);
        persistentStorage.saveAuthorizationId(initAuthorizationResponse.getAuthorisationId());
        return initAuthorizationResponse;
    }

    private Pair<ScaMethodEntity, ChallengeDataEntity> getScaMethodDetails(
            InitAuthorizationResponse initAuthorizationResponse) throws SupplementalInfoException {
        List<ScaMethodEntity> scaMethods = initAuthorizationResponse.getScaMethods();
        switch (scaMethods.size()) {
            case 0:
                throw new IllegalStateException(ErrorMessages.MISSING_SCA_METHOD_DETAILS);
            case 1:
                return new ImmutablePair<>(
                        initAuthorizationResponse.getScaMethods().get(0),
                        initAuthorizationResponse.getChallengeData());
            default:
                return getScaMethodDetailsOutOfMultiplePossible(scaMethods);
        }
    }

    private Pair<ScaMethodEntity, ChallengeDataEntity> getScaMethodDetailsOutOfMultiplePossible(
            List<ScaMethodEntity> scaMethods) throws SupplementalInfoException {
        ScaMethodEntity chosenScaMethod = collectScaMethod(scaMethods);
        SelectAuthenticationMethodResponse selectAuthenticationMethodResponse =
                apiClient.selectAuthorizationMethod(
                        persistentStorage.getConsentId(),
                        persistentStorage.getAuthorizationId(),
                        chosenScaMethod.getAuthenticationMethodId());

        if (selectAuthenticationMethodResponse.getChallengeData() == null) {
            throw new IllegalStateException(ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        } else {
            return new ImmutablePair<>(
                    selectAuthenticationMethodResponse.getChosenScaMethod(),
                    selectAuthenticationMethodResponse.getChallengeData());
        }
    }

    private ScaMethodEntity collectScaMethod(List<ScaMethodEntity> scaMethods)
            throws SupplementalInfoException {
        Field scaMethodField = fieldBuilder.getChooseScaMethodField(scaMethods);
        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(scaMethodField);
        int selectedIndex =
                Integer.parseInt(supplementalInformation.get(scaMethodField.getName())) - 1;

        return scaMethods.get(selectedIndex);
    }

    private void authorizeConsentWithOtp(String otpType, int otpValueLength)
            throws AuthenticationException {
        String otp = collectOtp(otpType, otpValueLength);
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

    private String collectOtp(String otpType, int otpCodeLength) throws SupplementalInfoException {
        Field otpField = fieldBuilder.getOtpField(otpType, otpCodeLength);
        Map<String, String> supplementalInformation =
                supplementalInformationHelper.askSupplementalInformation(otpField);
        return supplementalInformation.get(otpField.getName());
    }
}

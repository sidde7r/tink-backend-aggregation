package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.detail.FieldBuilder;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
public class BankverlagAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private static final String FINALISED = "finalised";
    private static final String FAILED = "failed";

    protected static final String PSU_AUTHENTICATED = "psuAuthenticated";
    protected static final String SCA_METHOD_SELECTED = "scaMethodSelected";
    protected static final String EXEMPTED = "exempted";
    protected static final String STARTED = "started";

    private static final int MAX_POLL_ATTEMPTS = 100;

    protected final BankverlagApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;
    private final BankverlagStorage storage;
    protected final Credentials credentials;
    private final FieldBuilder fieldBuilder;
    private final String aspspId;
    private final String aspspName;

    public BankverlagAuthenticator(
            BankverlagApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            BankverlagStorage storage,
            Credentials credentials,
            Catalog catalog,
            String aspspId,
            String aspspName) {
        this.apiClient = apiClient;
        this.supplementalInformationController = supplementalInformationController;
        this.storage = storage;
        this.credentials = credentials;
        this.fieldBuilder = new FieldBuilder(Preconditions.checkNotNull(catalog));
        this.aspspId = aspspId;
        this.aspspName = aspspName;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate() {
        String consentId = storage.getConsentId();
        if (Strings.isNullOrEmpty(consentId)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        verifyConsentValidity(consentId);
    }

    private void verifyConsentValidity(String consentId) {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails(consentId);
        if (!consentDetailsResponse.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        LocalDate now = LocalDate.now();
        log.info(
                "setting credentials expiry to: {} (now is: {})",
                consentDetailsResponse.getValidUntil(),
                now);
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    @Override
    public void authenticate(Credentials credentials) {
        validateInput(credentials);

        ConsentResponse consentResponse = createAndSaveConsent();

        initializeAuthorization(consentResponse.getLinks(), credentials);
        verifyConsentValidity(consentResponse.getConsentId());
    }

    protected void initializeAuthorization(LinksEntity linkEntity, Credentials credentials) {
        AuthorizationResponse authResponseAfterLogin =
                apiClient.initializeAuthorization(
                        linkEntity.getStartAuthorisationWithPsuAuthentication(),
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        authorize(authResponseAfterLogin);
    }

    protected void validateInput(Credentials credentials) {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), this.getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));
        validateCredentialPresent(credentials, Field.Key.USERNAME);
        validateCredentialPresent(credentials, Field.Key.PASSWORD);
    }

    private void validateCredentialPresent(Credentials credentials, Field.Key key) {
        if (Strings.isNullOrEmpty(credentials.getField(key))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private ConsentResponse createAndSaveConsent() {
        ConsentResponse consentResponse = apiClient.createConsent();
        storage.saveConsentId(consentResponse.getConsentId());
        return consentResponse;
    }

    private void authorize(AuthorizationResponse authResponseAfterLogin) {
        switch (authResponseAfterLogin.getScaStatus()) {
            case PSU_AUTHENTICATED:
                authorizeWithSelectedMethod(
                        pickMethodOutOfMultiplePossible(authResponseAfterLogin));
                break;
            case STARTED:
            case SCA_METHOD_SELECTED:
                authorizeWithSelectedMethod(authResponseAfterLogin);
                break;
            default:
                throw new IllegalStateException(
                        "Unexpected ScaStatus during consent authorization "
                                + authResponseAfterLogin.getScaStatus());
        }
    }

    protected AuthorizationResponse pickMethodOutOfMultiplePossible(
            AuthorizationResponse authResponseAfterLogin) {
        List<ScaMethodEntity> supportedScaMethods = getSupportedScaMethods(authResponseAfterLogin);
        ScaMethodEntity chosenScaMethod = collectScaMethod(supportedScaMethods);

        AuthorizationResponse authorizationResponse =
                apiClient.selectAuthorizationMethod(
                        authResponseAfterLogin.getLinks().getSelectAuthenticationMethod(),
                        chosenScaMethod.getAuthenticationMethodId());
        // VB do not respond ChosenScaMethod hence we set from what PSU selected.
        authorizationResponse.setChosenScaMethod(chosenScaMethod);
        return authorizationResponse;
    }

    protected List<ScaMethodEntity> getSupportedScaMethods(
            AuthorizationResponse authResponseAfterLogin) {
        List<ScaMethodEntity> methods = authResponseAfterLogin.getScaMethods();

        if (methods.isEmpty()) {
            throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.NO_SUPPORTED_METHOD_FOUND);
        }
        return methods;
    }

    protected ScaMethodEntity collectScaMethod(List<ScaMethodEntity> scaMethods) {
        if (scaMethods.size() == 1) {
            return scaMethods.get(0);
        }
        Field scaMethodField = fieldBuilder.getChooseScaMethodField(scaMethods, aspspId);
        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(scaMethodField);
        String selectedValue = supplementalInformation.get(scaMethodField.getName());
        if (StringUtils.isNumeric(selectedValue)) {
            int index = Integer.parseInt(selectedValue) - 1;
            if (index >= 0 && index < scaMethods.size()) {
                return scaMethods.get(index);
            }
        }
        throw SupplementalInfoError.NO_VALID_CODE.exception(
                "Could not map user input to list of available options.");
    }

    protected void authorizeWithSelectedMethod(AuthorizationResponse authorizationResponse) {
        String authenticationType = getAuthenticationType(authorizationResponse);

        log.info("User for authenticationType {} started 2FA", authenticationType);
        if ("PUSH_OTP".equalsIgnoreCase(authenticationType)) {
            authorizeWithPush(authorizationResponse);
        } else {
            authorizeWithOtp(authorizationResponse);
        }
        log.info("User for authenticationType {} successfully passed 2FA", authenticationType);
    }

    private String getAuthenticationType(AuthorizationResponse authorizationResponse) {

        if (authorizationResponse.getChosenScaMethod() != null) {
            return authorizationResponse.getChosenScaMethod().getAuthenticationType();
        }

        // BankVerlag doesn't send us info in chosenScaMethod field when user has only PUSH_OTP
        // (DECOUPLED) method available. This info is gathered from header stored in session storage

        if (storage.getPushOtpFromHeader() != null) {
            return storage.getPushOtpFromHeader();
        }
        throw LoginError.NOT_SUPPORTED.exception();
    }

    private void authorizeWithPush(AuthorizationResponse authorizationResponse) {
        showInfo(authorizationResponse.getChosenScaMethod());
        pollAuthorization(authorizationResponse.getLinks().getScaStatus());
    }

    private void showInfo(ScaMethodEntity scaMethod) {
        Field informationField = fieldBuilder.getInstructionsField(scaMethod, aspspName);
        try {
            supplementalInformationController.askSupplementalInformationSync(informationField);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void pollAuthorization(String url) {
        for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            AuthorizationResponse authorizationStatus = apiClient.getAuthorizationStatus(url);
            switch (authorizationStatus.getScaStatus()) {
                case FINALISED:
                case EXEMPTED:
                    return;
                case FAILED:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    break;
            }
        }
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    protected void authorizeWithOtp(AuthorizationResponse authorizationResponse) {

        if (authorizationResponse.getChallengeData() == null) {
            throw new IllegalStateException(ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        }

        String otp =
                collectOtp(
                        authorizationResponse.getChosenScaMethod(),
                        authorizationResponse.getChallengeData());
        AuthorizationResponse finalizeAuthorizationResponse =
                apiClient.finalizeAuthorization(
                        authorizationResponse.getLinks().getScaStatus(), otp);
        switch (finalizeAuthorizationResponse.getScaStatus()) {
            case FINALISED:
                break;
            case FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    protected String collectOtp(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = fieldBuilder.getOtpFields(scaMethod, challengeData);
        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(
                        fields.toArray(new Field[0]));
        String otp = supplementalInformation.get(fields.get(fields.size() - 1).getName());
        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        } else {
            return otp;
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
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
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.ScaMethodFilter;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.DecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.de.EmbeddedFieldBuilder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@Slf4j
@RequiredArgsConstructor
public class SparkassenAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private static final String FINALISED = "finalised";
    private static final String FAILED = "failed";

    protected static final String PSU_AUTHENTICATED = "psuAuthenticated";
    protected static final String SCA_METHOD_SELECTED = "scaMethodSelected";
    protected static final String EXEMPTED = "exempted";
    protected static final String STARTED = "started";

    private static final int MAX_POLL_ATTEMPTS = 100;

    protected final SparkassenApiClient apiClient;
    private final SupplementalInformationController supplementalInformationController;
    private final SparkassenStorage storage;
    protected final Credentials credentials;
    private final EmbeddedFieldBuilder embeddedFieldBuilder;
    private final DecoupledFieldBuilder decoupledFieldBuilder;
    private final ScaMethodFilter scaMethodFilter;

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
                "[SparkassenAuthDebug]: setting credentials expiry to: {} (now is: {})",
                consentDetailsResponse.getValidUntil(),
                now);
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    @Override
    public void authenticate(Credentials credentials) {
        validateInput(credentials);

        ConsentResponse consentResponse = createAndSaveConsent();

        AuthorizationResponse authResponseAfterLogin =
                apiClient.initializeAuthorization(
                        consentResponse.getLinks().getStartAuthorisationWithPsuAuthentication(),
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        authorize(authResponseAfterLogin);
        verifyConsentValidity(consentResponse.getConsentId());
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

    protected void authorize(AuthorizationResponse authResponse) {
        switch (authResponse.getScaStatus()) {
            case PSU_AUTHENTICATED:
                authorize(pickMethodOutOfMultiplePossible(authResponse));
                break;
            case STARTED:
            case SCA_METHOD_SELECTED:
                authorizeWithSelectedMethod(authResponse);
                break;
            case EXEMPTED:
                log.info("SCA exempted!");
                break;
            default:
                throw new IllegalStateException(
                        "Unexpected ScaStatus during authorization " + authResponse.getScaStatus());
        }
    }

    protected AuthorizationResponse pickMethodOutOfMultiplePossible(
            AuthorizationResponse authResponseAfterLogin) {
        List<ScaMethodEntity> usableScaMethods =
                scaMethodFilter.getUsableScaMethods(authResponseAfterLogin.getScaMethods());

        if (usableScaMethods.isEmpty()) {
            throw LoginError.NOT_SUPPORTED.exception(ErrorMessages.NO_SUPPORTED_METHOD_FOUND);
        }
        ScaMethodEntity chosenScaMethod = collectScaMethod(usableScaMethods);

        return apiClient.selectAuthorizationMethod(
                authResponseAfterLogin.getLinks().getSelectAuthenticationMethod(),
                chosenScaMethod.getAuthenticationMethodId());
    }

    protected ScaMethodEntity collectScaMethod(List<ScaMethodEntity> scaMethods) {
        if (scaMethods.size() == 1) {
            return scaMethods.get(0);
        }
        Field scaMethodField = embeddedFieldBuilder.getChooseScaMethodField(scaMethods);
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
        String authenticationType =
                authorizationResponse.getChosenScaMethod().getAuthenticationType();
        log.info("[Sparkassen 2FA] User for authenticationType {} started 2FA", authenticationType);
        if ("PUSH_DEC".equalsIgnoreCase(authenticationType)) {
            authorizeWithPush(authorizationResponse);
        } else {
            authorizeWithOtp(authorizationResponse);
        }
        log.info(
                "[Sparkassen 2FA] User for authenticationType {} successfully passed 2FA",
                authenticationType);
    }

    private void authorizeWithPush(AuthorizationResponse authorizationResponse) {
        showInfo(authorizationResponse.getChosenScaMethod());
        pollAuthorization(authorizationResponse.getLinks().getScaStatus());
    }

    private void showInfo(ScaMethodEntity scaMethod) {
        List<Field> informationFields = decoupledFieldBuilder.getInstructionsField(scaMethod);
        try {
            supplementalInformationController.askSupplementalInformationSync(
                    informationFields.toArray(new Field[0]));
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
        AuthorizationStatusResponse authorizationStatusResponse =
                apiClient.finalizeAuthorization(
                        authorizationResponse.getLinks().getAuthoriseTransaction(), otp);
        switch (authorizationStatusResponse.getScaStatus().getValue()) {
            case FINALISED:
                break;
            case FAILED:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.NOT_SUPPORTED.exception();
        }
    }

    protected String collectOtp(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = embeddedFieldBuilder.getOtpFields(scaMethod, challengeData);
        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(
                        fields.toArray(new Field[0]));
        String inputFieldName =
                fields.stream()
                        .filter(f -> !f.isImmutable())
                        .map(Field::getName)
                        .findFirst()
                        .orElse(null);
        String otp = supplementalInformation.get(inputFieldName);
        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        } else {
            return otp;
        }
    }
}

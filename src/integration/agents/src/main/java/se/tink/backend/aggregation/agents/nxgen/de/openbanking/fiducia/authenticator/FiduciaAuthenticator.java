package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.TanBuilder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;

@AllArgsConstructor
@Slf4j
public class FiduciaAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator, PaymentAuthenticator {

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode\\s\\\"(\\d+)");

    private static final String PSU_AUTHENTICATED = "psuAuthenticated";
    private static final String STARTED = "started";
    private static final String FINALISED = "finalised";
    private static final String EXEMPTED = "exempted";
    private static final int PSU_ID_MAX_ALLOWED_LENGTH = 30;

    private static final List<String> UNSUPPORTED_AUTH_METHOD_IDS =
            ImmutableList.of("972", "982"); // These two numbers are optical chiptan and photo tan
    private final Credentials credentials;
    private final FiduciaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate() {
        String consentId =
                persistentStorage
                        .get(StorageKeys.CONSENT_ID, String.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        verifyConsentValidity(consentId);
    }

    private void verifyConsentValidity(String consentId) {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails(consentId);
        if (!consentDetailsResponse.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    @Override
    public void authenticate(Credentials credentials) {
        validateCredentials(credentials);

        ConsentResponse consentResponse =
                createAndSaveConsent(credentials.getField(CredentialKeys.PSU_ID));
        AuthorizationResponse authorizationResponse =
                apiClient.authorizeWithPassword(
                        consentResponse
                                .getLinks()
                                .getStartAuthorisationWithPsuAuthentication()
                                .getHref(),
                        credentials.getField(Field.Key.PASSWORD));
        authorize(authorizationResponse);

        verifyConsentValidity(consentResponse.getConsentId());
    }

    @Override
    public void authenticatePayment(LinksEntity scaLinks) {
        validateCredentials(credentials);
        AuthorizationResponse authorizationResponse =
                apiClient.authorizeWithPassword(
                        scaLinks.getStartAuthorisationWithPsuAuthentication().getHref(),
                        credentials.getField(Field.Key.PASSWORD));
        authorize(authorizationResponse);
    }

    private void validateCredentials(Credentials credentials) {
        String username = credentials.getField(CredentialKeys.PSU_ID);
        String password = credentials.getField(Field.Key.PASSWORD);
        if (StringUtils.isBlank(username)
                || username.length() > PSU_ID_MAX_ALLOWED_LENGTH
                || !username.equals(username.trim())
                || StringUtils.isBlank(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private ConsentResponse createAndSaveConsent(String username) {
        ConsentResponse consentResponse = apiClient.createConsent(username);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return consentResponse;
    }

    private void authorize(AuthorizationResponse authorizationResponse) {
        switch (authorizationResponse.getScaStatus()) {
            case PSU_AUTHENTICATED:
                authorize(selectMethod(authorizationResponse));
                break;
            case STARTED:
                authorizeWithOtp(authorizationResponse);
                break;
            case EXEMPTED:
                break;
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        "Unexpected scaStatus during authorization ["
                                + authorizationResponse.getScaStatus()
                                + "]");
        }
    }

    private AuthorizationResponse selectMethod(AuthorizationResponse authorizationResponse) {
        List<ScaMethodEntity> onlySupportedScaMethods =
                authorizationResponse.getScaMethods().stream()
                        .filter(x -> !isUnsupportedMethod(x))
                        .collect(Collectors.toList());

        if (onlySupportedScaMethods.isEmpty()) {
            throwNoSupportedMethodFound();
        }

        ScaMethodEntity selectedMethod = askUserForSelection(onlySupportedScaMethods);
        AuthorizationResponse authorizationResponseAfterMethodSelection =
                apiClient.selectAuthMethod(
                        authorizationResponse.getLinks().getSelectAuthenticationMethod().getHref(),
                        selectedMethod.getAuthenticationMethodId());

        // Fiducia does not include the selected method in response of previous request
        // set it here to keep processing consistent
        authorizationResponseAfterMethodSelection.setChosenScaMethod(selectedMethod);
        return authorizationResponseAfterMethodSelection;
    }

    private ScaMethodEntity askUserForSelection(List<ScaMethodEntity> onlySupportedScaMethods) {
        if (onlySupportedScaMethods.size() == 1) {
            return onlySupportedScaMethods.get(0);
        }

        Field scaMethodField =
                CommonFields.Selection.build(
                        catalog,
                        null,
                        GermanFields.SelectOptions.prepareSelectOptions(
                                onlySupportedScaMethods, new FiduciaIconUrlMapper()));
        String selectedValue =
                supplementalInformationController
                        .askSupplementalInformationSync(scaMethodField)
                        .get(scaMethodField.getName());

        if (StringUtils.isNumeric(selectedValue)) {
            int index = Integer.parseInt(selectedValue) - 1;
            if (index >= 0 && index < onlySupportedScaMethods.size()) {
                return onlySupportedScaMethods.get(index);
            }
        }
        throw SupplementalInfoError.NO_VALID_CODE.exception(
                "Could not map user input to list of available options.");
    }

    private void authorizeWithOtp(AuthorizationResponse authorizationResponse) {
        ScaMethodEntity chosenScaMethod = authorizationResponse.getChosenScaMethod();
        if (isUnsupportedMethod(chosenScaMethod)) {
            throwNoSupportedMethodFound();
        }

        log.info(
                "[Fiducia 2FA] User for authenticationType {} started 2FA",
                chosenScaMethod.getAuthenticationType());

        AuthorizationStatusResponse scaStatusResponse =
                apiClient.authorizeWithOtp(
                        authorizationResponse.getLinks().getAuthoriseTransaction().getHref(),
                        collectOtp(authorizationResponse));
        if (!FINALISED.equalsIgnoreCase(scaStatusResponse.getScaStatus())) {
            throw LoginError.DEFAULT_MESSAGE.exception("Invalid sca status");
        }

        log.info(
                "[Fiducia 2FA] User for authenticationType {} successfully passed 2FA",
                chosenScaMethod.getAuthenticationType());
    }

    private String collectOtp(AuthorizationResponse authorizationResponse) {
        ScaMethodEntity chosenScaMethod = authorizationResponse.getChosenScaMethod();
        List<Field> fields = new LinkedList<>();
        extractStartcode(authorizationResponse)
                .ifPresent(x -> fields.add(GermanFields.Startcode.build(catalog, x)));
        String authenticationType = chosenScaMethod.getAuthenticationType();

        TanBuilder tanBuilder =
                GermanFields.Tan.builder(catalog)
                        .authenticationType(authenticationType)
                        .otpMinLength(6)
                        .otpMaxLength(6)
                        .authenticationMethodName(chosenScaMethod.getName());

        ChallengeDataEntity challengeData = authorizationResponse.getChallengeData();
        if (challengeData != null) {
            tanBuilder.otpFormat(challengeData.getOtpFormat());
        }
        fields.add(tanBuilder.build());

        String otp =
                supplementalInformationController
                        .askSupplementalInformationSync(fields.toArray(new Field[0]))
                        .get(fields.get(fields.size() - 1).getName());

        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        }
        return otp;
    }

    private Optional<String> extractStartcode(AuthorizationResponse authorizationResponse) {
        return Optional.ofNullable(authorizationResponse.getChallengeData())
                .map(ChallengeDataEntity::getAdditionalInformation)
                .map(this::extractStartCodeFromChallengeString);
    }

    private String extractStartCodeFromChallengeString(String challengeString) {
        Matcher matcher = STARTCODE_CHIP_PATTERN.matcher(challengeString);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isUnsupportedMethod(ScaMethodEntity scaMethod) {
        return Optional.ofNullable(scaMethod)
                .map(ScaMethodEntity::getAuthenticationMethodId)
                .filter(UNSUPPORTED_AUTH_METHOD_IDS::contains)
                .isPresent();
    }

    private void throwNoSupportedMethodFound() {
        throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
    }
}

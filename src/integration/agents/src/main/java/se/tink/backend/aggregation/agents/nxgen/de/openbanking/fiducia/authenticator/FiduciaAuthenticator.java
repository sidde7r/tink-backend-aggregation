package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.ChallengeData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

@AllArgsConstructor
@Slf4j
public class FiduciaAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private static final String STATIC_SALT = "laet<iecu7aYuPhee8Oe";
    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode\\s\\\"(\\d+)");

    private static final String PSU_AUTHENTICATED = "psuAuthenticated";
    private static final String STARTED = "started";
    private static final String FINALISED = "finalised";
    private static final int PSU_ID_MAX_ALLOWED_LENGTH = 30;

    private static final List<String> UNSUPPORTED_AUTH_METHOD_IDS =
            ImmutableList.of("972", "982"); // These two numbers are optical chiptan and photo tan
    private final Credentials credentials;
    private final FiduciaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate() throws SessionException, LoginException, AuthorizationException {
        String consentId =
                persistentStorage
                        .get(StorageKeys.CONSENT_ID, String.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails(consentId);

        if (!consentDetailsResponse.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    @Override
    public void authenticate(Credentials credentials) throws SupplementalInfoException {
        String username = credentials.getField(CredentialKeys.PSU_ID);
        validateUsername(username);
        String password = credentials.getField(CredentialKeys.PASSWORD);
        logHashes(credentials);
        sessionStorage.put(StorageKeys.PSU_ID, username);

        String consentId = apiClient.createConsent();
        ScaResponse scaResponse = apiClient.authorizeConsent(consentId, password);
        ScaStatusResponse scaStatusResponse = authorizeWithSca(scaResponse);

        if (!FINALISED.equalsIgnoreCase(scaStatusResponse.getScaStatus())) {
            throw LoginError.DEFAULT_MESSAGE.exception("Invalid sca status");
        }

        ConsentDetailsResponse detailsResponse = apiClient.getConsentDetails(consentId);

        if (!detailsResponse.isValid()) {
            throw LoginError.DEFAULT_MESSAGE.exception("Invalid consent status");
        }

        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        credentials.setSessionExpiryDate(detailsResponse.getValidUntil());
    }

    private void validateUsername(String username) {
        if (StringUtils.isBlank(username) || username.length() > PSU_ID_MAX_ALLOWED_LENGTH) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void logHashes(Credentials credentials) {
        // There are a lot of invalid_credentials thrown.
        // Users often finally manages to provide correct credentials in 2nd or 3rd attempt.
        // We want to investigate if users have problems with providing username or password.
        // To achieve that - this logging will be helpful. We will check the hashes from
        // unsuccessful and successful authentications for the same credentialsId / userId and check
        // whether username hash or credentials hash changed.
        log.info(
                "[Fiducia Auth] Hashes: {}, {}",
                ENCODER.encodeToString(
                                Hash.sha512(
                                        credentials.getField(CredentialKeys.PSU_ID) + STATIC_SALT))
                        .substring(0, 6),
                ENCODER.encodeToString(
                                Hash.sha512(
                                        credentials.getField(CredentialKeys.PASSWORD)
                                                + STATIC_SALT))
                        .substring(0, 6));
    }

    private ScaStatusResponse authorizeWithSca(ScaResponse scaResponse) {
        switch (scaResponse.getScaStatus()) {
            case PSU_AUTHENTICATED:
                return selectMethod(scaResponse);
            case STARTED:
                return authorizeWithOtp(scaResponse);
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        "Unexpected scaStatus during authorization ["
                                + scaResponse.getScaStatus()
                                + "]");
        }
    }

    private ScaStatusResponse selectMethod(ScaResponse scaResponse) {
        List<ScaMethod> onlySupportedScaMethods =
                scaResponse.getScaMethods().stream()
                        .filter(x -> !isUnsupportedMethod(x))
                        .collect(Collectors.toList());

        if (onlySupportedScaMethods.isEmpty()) {
            throwNoSupportedMethodFound();
        }

        ScaResponse scaSelectionResponse =
                apiClient.selectAuthMethod(
                        scaResponse.getLinks().getSelectAuthenticationMethod(),
                        askUserForSelection(onlySupportedScaMethods).getAuthenticationMethodId());

        return authorizeWithOtp(scaSelectionResponse);
    }

    private ScaMethod askUserForSelection(List<ScaMethod> onlySupportedScaMethods) {
        if (onlySupportedScaMethods.size() == 1) {
            return onlySupportedScaMethods.get(0);
        }

        Field scaMethodField =
                CommonFields.Selection.build(
                        catalog,
                        onlySupportedScaMethods.stream()
                                .map(ScaMethod::getName)
                                .collect(Collectors.toList()));
        String index =
                supplementalInformationHelper
                        .askSupplementalInformation(scaMethodField)
                        .get(scaMethodField.getName());

        int selectedIndex = Integer.parseInt(index) - 1;
        return onlySupportedScaMethods.get(selectedIndex);
    }

    private ScaStatusResponse authorizeWithOtp(ScaResponse scaResponse) {
        List<Field> fields = new LinkedList<>();
        Optional<String> startcode = extractStartcode(scaResponse);

        ScaMethod chosenScaMethod = scaResponse.getChosenScaMethod();

        if (!startcode.isPresent() && isUnsupportedMethod(chosenScaMethod)) {
            throwNoSupportedMethodFound();
        }
        startcode.ifPresent(x -> fields.add(GermanFields.Startcode.build(catalog, x)));

        ChallengeData challengeData = scaResponse.getChallengeData();
        fields.add(
                GermanFields.Tan.build(
                        catalog,
                        chosenScaMethod != null ? chosenScaMethod.getAuthenticationType() : null,
                        chosenScaMethod != null ? chosenScaMethod.getName() : null,
                        challengeData != null ? challengeData.getOtpMaxLength() : null,
                        challengeData != null ? challengeData.getOtpFormat() : null));

        String otpCode =
                supplementalInformationHelper
                        .askSupplementalInformation(fields.toArray(new Field[0]))
                        .get(fields.get(fields.size() - 1).getName());

        String authoriseTransactionHref = scaResponse.getLinks().getAuthoriseTransaction();
        return apiClient.authorizeWithOtpCode(authoriseTransactionHref, otpCode);
    }

    private Optional<String> extractStartcode(ScaResponse scaResponse) {
        return Optional.ofNullable(scaResponse.getChallengeData())
                .map(ChallengeData::getAdditionalInformation)
                .map(this::extractStartCodeFromChallengeString);
    }

    private String extractStartCodeFromChallengeString(String challengeString) {
        Matcher matcher = STARTCODE_CHIP_PATTERN.matcher(challengeString);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isUnsupportedMethod(ScaMethod scaMethod) {
        return Optional.ofNullable(scaMethod)
                .map(ScaMethod::getAuthenticationMethodId)
                .filter(UNSUPPORTED_AUTH_METHOD_IDS::contains)
                .isPresent();
    }

    private void throwNoSupportedMethodFound() {
        throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
    }
}

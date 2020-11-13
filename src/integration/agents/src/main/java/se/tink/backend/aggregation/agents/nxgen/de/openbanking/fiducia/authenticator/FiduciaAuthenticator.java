package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
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
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

@AllArgsConstructor
public class FiduciaAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private static final Pattern STARTCODE_CHIP_PATTERN = Pattern.compile("Startcode\\s\\\"(\\d+)");

    private static final String PSU_AUTHENTICATED = "psuAuthenticated";
    private static final String STARTED = "started";
    private static final String FINALISED = "finalised";

    private static final List<String> UNSUPPORTED_AUTH_METHOD_IDS =
            ImmutableList.of("972", "982"); // These two numbers are optical chiptan and photo tan
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

        if (!apiClient.getConsentStatus(consentId).isAcceptedStatus()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void authenticate(Credentials credentials) throws SupplementalInfoException {
        String username = credentials.getField(CredentialKeys.PSU_ID);
        String password = credentials.getField(CredentialKeys.PASSWORD);
        sessionStorage.put(StorageKeys.PSU_ID, username);

        String consentId = apiClient.createConsent();
        ScaResponse scaResponse = apiClient.authorizeConsent(consentId, password);
        ScaStatusResponse scaStatusResponse = authorizeWithSca(scaResponse);
        if (FINALISED.equalsIgnoreCase(scaStatusResponse.getScaStatus())) {
            persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        } else {
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
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

        if (!startcode.isPresent() && isUnsupportedMethod(scaResponse.getChosenScaMethod())) {
            throwNoSupportedMethodFound();
        }

        startcode.ifPresent(x -> fields.add(GermanFields.Startcode.build(catalog, x)));
        fields.add(
                GermanFields.Tan.build(
                        catalog,
                        Optional.ofNullable(scaResponse.getChosenScaMethod())
                                .map(ScaMethod::getName)
                                .orElse(null)));

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

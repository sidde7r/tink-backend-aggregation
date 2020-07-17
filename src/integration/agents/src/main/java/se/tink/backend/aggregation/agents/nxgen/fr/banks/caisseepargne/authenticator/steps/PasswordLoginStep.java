package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.MembershipTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.RequestValues.ValidationTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities.ValidationUnit;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.OAuth2V2AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.SamlAuthnResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.utils.ImageRecognizer;
import se.tink.libraries.streamutils.StreamUtils;

public class PasswordLoginStep extends AbstractAuthenticationStep {
    private final CaisseEpargneApiClient apiClient;
    private final Storage instanceStorage;
    private final PersistentStorage persistentStorage;
    public static final String STEP_ID = "passwordLoginStep";

    public PasswordLoginStep(
            CaisseEpargneApiClient apiClient,
            Storage instanceStorage,
            PersistentStorage persistentStorage) {
        super(STEP_ID);
        this.apiClient = apiClient;
        this.instanceStorage = instanceStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final String password =
                Strings.emptyToNull(request.getCredentials().getField(Field.Key.PASSWORD));
        final String username =
                Strings.emptyToNull(request.getCredentials().getField(Key.USERNAME));
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);

        apiClient.getOAuth2Token();
        IdentificationRoutingResponse identificationRoutingResponse =
                apiClient.identificationRouting(username);
        if (!identificationRoutingResponse.isValid()) {
            throw new IllegalStateException("Invalid routing response");
        }
        String bankId = identificationRoutingResponse.getBankId();
        instanceStorage.put(StorageKeys.BANK_ID, bankId);
        OAuth2V2AuthorizeResponse oAuth2V2AuthorizeResponse =
                apiClient.oAuth2Authorize(
                        username, bankId, identificationRoutingResponse.getMembershipTypeValue());
        if (!oAuth2V2AuthorizeResponse.isValid()) {
            throw new IllegalStateException("OAuth Authorize response was not valid.");
        }
        String samlTransactionPath =
                apiClient.getSamlTransactionPath(
                        new URL(oAuth2V2AuthorizeResponse.getAction()),
                        oAuth2V2AuthorizeResponse.getSAMLRequest());
        SamlAuthnResponse samlAuthnResponse = apiClient.samlAuthorize(samlTransactionPath);
        samlAuthnResponse.throwIfFailedAuthentication();
        instanceStorage.put(StorageKeys.SAML_TRANSACTION_PATH, samlTransactionPath);
        String validationId =
                samlAuthnResponse
                        .getValidationId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Not able to determine validation id."));
        String validationUnitId =
                samlAuthnResponse
                        .getValidationUnitId()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Not able to determine validation unit id."));

        String passwordString =
                getPasswordString(identificationRoutingResponse, samlAuthnResponse, password);

        SamlAuthnResponse passwordResponse =
                apiClient.submitPassword(
                        validationId, validationUnitId, passwordString, samlTransactionPath);
        passwordResponse.throwIfFailedAuthentication();
        instanceStorage.put(StorageKeys.CREDENTIALS_RESPONSE, passwordResponse);
        if (passwordResponse.isStillAuthenticating()) {
            persistentStorage.put(StorageKeys.COULD_AUTO_AUTHENTICATE, false);
            String type =
                    Strings.nullToEmpty(
                            passwordResponse.getValidationUnits().stream()
                                    .map(ValidationUnit::getType)
                                    .collect(StreamUtils.toSingleton()));
            if (ValidationTypes.OTP.getName().equalsIgnoreCase(type)) {
                return AuthenticationStepResponse.executeStepWithId(SmsOtpStep.STEP_ID);
            } else {
                throw LoginError.NOT_SUPPORTED.exception();
            }
        }
        persistentStorage.put(StorageKeys.COULD_AUTO_AUTHENTICATE, true);
        return AuthenticationStepResponse.executeStepWithId(FinalizeAuthStep.STEP_ID);
    }

    private String getPasswordString(
            IdentificationRoutingResponse identificationRoutingResponse,
            SamlAuthnResponse samlAuthnResponse,
            String password) {
        String passwordString;
        if (MembershipTypes.PRO.equals(identificationRoutingResponse.getMembershipType())) {
            String imagesUrl =
                    samlAuthnResponse
                            .getKeyboardImagesUrl()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Path to keyboard images could not be determined."));
            passwordString = getVirtualKeyboardPassword(password, imagesUrl);
        } else if (MembershipTypes.PART.equals(identificationRoutingResponse.getMembershipType())) {
            passwordString = password;
        } else {
            throw new IllegalStateException(
                    "Could not determine authentication method for membership type: "
                            + identificationRoutingResponse.getMembershipTypeLabel()
                            + ", code: "
                            + identificationRoutingResponse.getMembershipTypeCode());
        }
        return passwordString;
    }

    private String getVirtualKeyboardPassword(String password, String imagesUrl) {
        Map<String, byte[]> keyboardImages =
                apiClient.getKeyboardImages(Urls.ICG_AUTH_BASE.concat(imagesUrl));
        Map<Integer, String> digitKeyMap =
                keyboardImages.entrySet().stream()
                        .map(this::convertImageToDigitKeyMap)
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return convertPasswordToKeyString(password, digitKeyMap);
    }

    private Entry<Integer, String> convertImageToDigitKeyMap(Entry<String, byte[]> keyboardImage) {
        int parsedDigit =
                Integer.parseInt(
                        ImageRecognizer.ocr(keyboardImage.getValue(), Color.WHITE)
                                .replaceAll("\\s", ""));
        return new SimpleEntry<>(parsedDigit, keyboardImage.getKey());
    }

    private String convertPasswordToKeyString(String password, Map<Integer, String> digitToKeyMap) {
        List<Integer> passwordDigits =
                password.chars()
                        .mapToObj(c -> (char) c)
                        .map(Character::getNumericValue)
                        .collect(Collectors.toList());
        return passwordDigits.stream().map(digitToKeyMap::get).collect(Collectors.joining(" "));
    }
}

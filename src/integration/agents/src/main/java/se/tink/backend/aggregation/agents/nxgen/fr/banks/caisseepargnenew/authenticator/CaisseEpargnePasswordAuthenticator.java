package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator;

import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.MembershipTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.OAuth2V2AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.SamlAuthnResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.ImageRecognizer;

public class CaisseEpargnePasswordAuthenticator implements PasswordAuthenticator {

    private final CaisseEpargneApiClient apiClient;

    public CaisseEpargnePasswordAuthenticator(CaisseEpargneApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        apiClient.getOAuth2Token();
        IdentificationRoutingResponse identificationRoutingResponse =
                apiClient.identificationRouting(username);
        if (!identificationRoutingResponse.isValid()) {
            throw new IllegalStateException("Invalid routing response");
        }
        String bankId = identificationRoutingResponse.getBankId();
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
        apiClient.oAuth2Consume(
                passwordResponse
                        .getSaml2PostAction()
                        .orElseThrow(
                                () -> new IllegalStateException("SAML action URL is missing.")),
                passwordResponse
                        .getSamlResponseValue()
                        .orElseThrow(() -> new IllegalStateException("SAML response missing.")));

        apiClient.soapActionSsoBapi(bankId);
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
}

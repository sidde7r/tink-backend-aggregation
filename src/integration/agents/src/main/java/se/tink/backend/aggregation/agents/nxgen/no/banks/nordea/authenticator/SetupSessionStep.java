package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class SetupSessionStep implements AuthenticationStep {

    private static final LocalizableKey VALIDATE_REFERENCE_CODE_MSG =
            new LocalizableKey(
                    "Please check if given reference code matches with presented one for Bank ID");

    private final AuthenticationClient authenticationClient;
    private final NordeaNoStorage storage;
    private final RandomValueGenerator randomValueGenerator;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        // Since steppable authenticator works in a mysterious way, it will rerun this step after
        // gathering supplementalInfo
        // This will move to next step, as it is supposed to.
        if (!request.getUserInputs().isEmpty()) {
            return AuthenticationStepResponse.executeNextStep();
        }

        Credentials credentials = request.getCredentials();

        String state = randomValueGenerator.generateRandomBase64UrlEncoded(26);
        String nonce = randomValueGenerator.generateRandomBase64UrlEncoded(26);
        String codeVerifier = randomValueGenerator.generateRandomBase64UrlEncoded(86);
        String codeChallenge = calculateCodeChallenge(codeVerifier);

        storage.storeCodeVerifier(codeVerifier);

        AuthenticationsResponse authenticationResponse =
                authenticationClient.getNordeaSessionDetails(codeChallenge, state, nonce);
        String sessionId = authenticationResponse.getSessionId();
        storage.storeSessionId(sessionId);

        /*
        This request redirects user from Nordea to OIDC service (one of BankID providers) where the OIDC session
        will be initialized. The first response from OIDC sets a session activation cookie that is required in
        later steps. Since user may have disabled cookies in their browser, this response contains some JS to check
        if that's the case and prompt user to enable cookies if required. Since our client accepts all cookies we
        just need to continue authentication with url that's embedded in response meta tags.
         */
        HttpResponse oidcSessionActivationResponse =
                authenticationClient.initializeOidcSession(
                        codeChallenge,
                        state,
                        nonce,
                        authenticationResponse.getBankidIntegrationUrl(),
                        sessionId,
                        credentials);
        String continueOidcAuthenticationUrl =
                OidcSessionHelper.extractContinueOidcAuthUrl(oidcSessionActivationResponse);

        HttpResponse bankIdInitializationResponse =
                authenticationClient.getUrl(continueOidcAuthenticationUrl);

        OidcSessionDetails oidcSessionDetails =
                OidcSessionHelper.extractBankIdSessionDetails(bankIdInitializationResponse);

        if (!oidcSessionDetails.isInProperState()) {
            OidcSessionHelper.throwBankIdError(oidcSessionDetails.getErrorCode());
        }

        String oidcSessionId = oidcSessionDetails.getSessionId();
        storage.storeOidcSessionId(oidcSessionId);
        displayBankIdPrompt(oidcSessionDetails.getMerchantReference());

        return AuthenticationStepResponse.executeNextStep();
    }

    private String calculateCodeChallenge(String codeVerifier) {
        return EncodingUtils.encodeAsBase64UrlSafe(Hash.sha256(codeVerifier));
    }

    private void displayBankIdPrompt(String referenceNumber) {
        Field field = getBankIdPhraseVerificationField(referenceNumber);

        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private Field getBankIdPhraseVerificationField(String referenceNumber) {
        return Field.builder()
                .immutable(true)
                .description(catalog.getString(VALIDATE_REFERENCE_CODE_MSG))
                .value(referenceNumber)
                .name("name")
                .build();
    }
}

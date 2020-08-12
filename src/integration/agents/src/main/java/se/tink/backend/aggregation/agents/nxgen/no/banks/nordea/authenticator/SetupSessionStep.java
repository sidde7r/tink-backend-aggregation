package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@AllArgsConstructor
public class SetupSessionStep implements AuthenticationStep {

    private AuthenticationClient authenticationClient;
    private NordeaNoStorage storage;
    private RandomValueGenerator randomValueGenerator;

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

        HttpResponse bankIdInitializationResponse =
                authenticationClient.initializeOidcSession(
                        codeChallenge,
                        state,
                        nonce,
                        authenticationResponse.getBankidIntegrationUrl(),
                        sessionId,
                        credentials);

        OidcSessionDetails oidcSessionDetails =
                extractBankIdSessionDetails(bankIdInitializationResponse);

        if (!oidcSessionDetails.isInProperState()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        String oidcSessionId = oidcSessionDetails.getSessionId();
        storage.storeOidcSessionId(oidcSessionId);
        List<Field> confirm =
                Collections.singletonList(
                        getBankIdPhraseVerificationField(
                                oidcSessionDetails.getMerchantReference()));

        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder().withFields(confirm).build());
    }

    private String calculateCodeChallenge(String codeVerifier) {
        return EncodingUtils.encodeAsBase64UrlSafe(Hash.sha256(codeVerifier));
    }

    private OidcSessionDetails extractBankIdSessionDetails(
            HttpResponse bankIdInitializationResponse) {
        Document oidcSessionInfo = Jsoup.parse(bankIdInitializationResponse.getBody(String.class));
        Map<String, String> metaElements =
                oidcSessionInfo.getElementsByTag("meta").stream()
                        .filter(x -> x.attr("name") != null && x.attr("name").startsWith("oidc-"))
                        .collect(Collectors.toMap(x -> x.attr("name"), z -> z.attr("content")));

        return new OidcSessionDetails(
                metaElements.get("oidc-action"),
                metaElements.get("oidc-error"),
                metaElements.get("oidc-errorCode"),
                metaElements.get("oidc-errorMessage"),
                metaElements.get("oidc-merchantReference"),
                metaElements.get("oidc-sid"));
    }

    private Field getBankIdPhraseVerificationField(String phrase) {
        return Field.builder()
                .immutable(true)
                .description("Please verify the BankId login.")
                .value(phrase)
                .name("name")
                .build();
    }
}

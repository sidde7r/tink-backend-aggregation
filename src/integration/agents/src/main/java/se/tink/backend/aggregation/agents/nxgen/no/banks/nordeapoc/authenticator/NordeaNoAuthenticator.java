package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import src.integration.bankid.BankIdOidcIframeAuthenticationService;

@Slf4j
@RequiredArgsConstructor
public class NordeaNoAuthenticator implements TypedAuthenticator {

    private final AuthenticationClient authenticationClient;
    private final NordeaNoStorage storage;
    private final RandomValueGenerator randomValueGenerator;
    private final BankIdOidcIframeAuthenticationService bankIdAuthenticationService;

    @Override
    @SneakyThrows
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String state = randomValueGenerator.generateRandomBase64UrlEncoded(26);
        String nonce = randomValueGenerator.generateRandomBase64UrlEncoded(26);
        String codeVerifier = randomValueGenerator.generateRandomBase64UrlEncoded(86);
        String codeChallenge = calculateCodeChallenge(codeVerifier);
        String deviceId = generateDeviceId();
        storage.storeDeviceId(deviceId);

        AuthenticationsResponse authenticationResponse =
                authenticationClient.initializeNordeaAuthentication(codeChallenge, state, nonce);

        // extract url which serves iframe
        HttpResponse iframeInitializationResponse =
                authenticationClient.getInitializeIFrameResponse(
                        codeChallenge,
                        state,
                        nonce,
                        authenticationResponse.getBankidIntegrationUrl(),
                        authenticationResponse.getSessionId());
        String iframeUrl = iframeInitializationResponse.getLocation().toString();

        String authorizationCode =
                bankIdAuthenticationService.displayIframeAndWaitForAuthorizationCode(iframeUrl);

        OauthTokenResponse oauthTokenResponse =
                authenticationClient.getOathToken(authorizationCode, codeVerifier);
        OAuth2Token oauthToken =
                oauthTokenResponse
                        .toOauthToken()
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        storage.storeOauthToken(oauthToken);
    }

    private String calculateCodeChallenge(String codeVerifier) {
        return EncodingUtils.encodeAsBase64UrlSafe(Hash.sha256(codeVerifier));
    }

    private String generateDeviceId() {
        return randomValueGenerator.getUUID().toString().toUpperCase();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}

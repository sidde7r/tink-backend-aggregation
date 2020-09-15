package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsPatchResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.CodeExchangeReqResp;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.retrypolicy.RetryCallback;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;

@Slf4j
public class VerifySessionStep implements AuthenticationStep {

    private static final int RETRY_ATTEMPTS = 40;
    private static final int RETRY_BACKOFF = 3000;

    private AuthenticationClient authenticationClient;
    private NordeaNoStorage storage;
    private RandomValueGenerator randomValueGenerator;

    private final RetryExecutor retryExecutor = new RetryExecutor();

    VerifySessionStep(
            AuthenticationClient authenticationClient,
            NordeaNoStorage storage,
            RandomValueGenerator randomValueGenerator) {
        this.authenticationClient = authenticationClient;
        this.storage = storage;
        this.randomValueGenerator = randomValueGenerator;
        this.retryExecutor.setRetryPolicy(new RetryPolicy(RETRY_ATTEMPTS, Throwable.class));
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        String sessionId = storage.retrieveSessionId();
        String oidcSessionId = storage.retrievOidcSessionId();
        String codeVerifier = storage.retrieveCodeVerifier();

        // Keep checking if approved by user
        pollOidcSession(oidcSessionId);

        // Query oidc site for some magic number
        HttpResponse oidcSessionResponse =
                authenticationClient.getBidCodeOfOidcSession(oidcSessionId);
        String bidCode = OidcSessionHelper.getBidCodeOrThrowIfNotPresent(oidcSessionResponse);

        // Patch the authentication in identify.nordea.com, sending them bidCode
        AuthenticationsPatchResponse authenticationsPatchResponse =
                authenticationClient.authenticationsPatch(bidCode, sessionId);

        // post authorization in identify.nordea.com, swapping one code for another
        CodeExchangeReqResp codeExchangeReqResp =
                authenticationClient.codeExchange(authenticationsPatchResponse.getCode());

        // Exchange that code for oauth token
        String deviceId = generateDeviceId();
        storage.storeDeviceId(deviceId);
        OauthTokenResponse oauthTokenResponse =
                authenticationClient.getOathToken(codeExchangeReqResp.getCode(), codeVerifier);
        OAuth2Token oauthToken =
                oauthTokenResponse
                        .toOauthToken()
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        storage.storeOauthToken(oauthToken);

        // good to go
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void pollOidcSession(String oidcSessionId) throws AuthenticationException {
        retryExecutor.execute(
                (RetryCallback<Void, AuthenticationException>)
                        () -> {
                            HttpResponse httpResponse =
                                    authenticationClient.checkOidcSession(oidcSessionId);
                            if (httpResponse.getStatus() == 204) {
                                backoffAWhile();
                                throw LoginError.DEFAULT_MESSAGE.exception();
                            }
                            return null;
                        });
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(RETRY_BACKOFF);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Woke up early", e);
        }
    }

    private String generateDeviceId() {
        return randomValueGenerator.getUUID().toString().toUpperCase();
    }
}

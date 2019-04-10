package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator;

import java.security.SecureRandom;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc.InitAuthRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc.InitAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc.PollAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class NordeaBankIDAuthenticator implements BankIdAuthenticator<InitAuthResponse> {

    private final NordeaBaseApiClient apiClient;
    private final NordeaSessionStorage sessionStorage;
    private final NordeaPersistentStorage persistentStorage;

    public NordeaBankIDAuthenticator(
            NordeaBaseApiClient apiClient,
            NordeaSessionStorage sessionStorage,
            NordeaPersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public InitAuthResponse init(String ssn)
            throws BankIdException, BankServiceException, AuthorizationException {
        // Start auth - POST on endpoint: /v3/authorize-decoupled
        String state = generateState();
        InitAuthRequest request =
                InitAuthRequest.create(persistentStorage.getRedirectUrl(), ssn, state);
        InitAuthResponse response = apiClient.initAuthorization(request);

        return response;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.empty();
    }

    @Override
    public BankIdStatus collect(InitAuthResponse reference)
            throws AuthenticationException, AuthorizationException {
        // Polling for auth code - get on endpoint: /v3/authorize-decoupled/{order_ref}
        String collectPath = reference.getCollectPath();

        PollAuthResponse pollResponse = null;
        try {

            // if we get a 200 back we are authorized
            pollResponse = apiClient.pollAuthCode(collectPath, reference.tppTokenAsHeaderValue());

        } catch (HttpResponseException hre) {
            int status = hre.getResponse().getStatus();

            switch (status) {
                case HttpStatus.SC_NOT_MODIFIED:
                    return BankIdStatus.WAITING;
                case HttpStatus.SC_REQUEST_TIMEOUT:
                    return BankIdStatus.CANCELLED;
                default:
                    throw hre;
            }
        }

        // Retrieve access token - POST on endpoint: /v3/authorize-decoupled/token
        // store access token in session storage
        TokenRequest request =
                new TokenRequest(pollResponse.getCode(), persistentStorage.getRedirectUrl());
        TokenResponse tokenResponse =
                apiClient.getAccessToken(
                        pollResponse.getTokenPath(), reference.tppTokenAsHeaderValue(), request);
        sessionStorage.setAccessToken(tokenResponse.toOauth2Token());

        return BankIdStatus.DONE;
    }

    private String generateState() {
        byte[] randomBytes = new byte[8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        return EncodingUtils.encodeHexAsString(randomBytes);
    }
}

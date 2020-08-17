package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class FinalKeyCardAuthenticationStep extends AbstractAuthenticationStep {
    public static final String STEP_ID = "final-key-card-step";

    private final SessionStorage sessionStorage;
    private final BecApiClient apiClient;
    private final String deviceId;
    private final PersistentStorage persistentStorage;

    public FinalKeyCardAuthenticationStep(
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            BecApiClient apiClient,
            String deviceId) {
        super(STEP_ID);
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
        this.deviceId = deviceId;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = request.getCredentials();

        String nemidChallenge = sessionStorage.get(StorageKeys.CHALLENGE_STORAGE_KEY);
        String challengeResponseValue =
                sessionStorage.get(StorageKeys.KEY_CARD_CHALLENGE_RESPONSE_KEY);
        String userName = credentials.getField(Key.USERNAME);
        String password = credentials.getField(Key.PASSWORD);

        LoggedInEntity loggedInEntity =
                apiClient.authKeyCard(
                        userName, password, challengeResponseValue, nemidChallenge, deviceId);
        persistentStorage.put(StorageKeys.SCA_TOKEN_STORAGE_KEY, loggedInEntity.getScaToken());

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }
}

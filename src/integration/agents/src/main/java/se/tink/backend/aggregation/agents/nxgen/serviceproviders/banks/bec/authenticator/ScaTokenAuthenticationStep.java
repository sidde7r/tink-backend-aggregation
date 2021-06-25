package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.UserAvailability;

@RequiredArgsConstructor
@Slf4j
public class ScaTokenAuthenticationStep implements AuthenticationStep {
    private final BecApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String deviceId;
    private final UserAvailability userAvailability;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (persistentStorage.containsKey(StorageKeys.SCA_TOKEN_STORAGE_KEY)
                && persistentStorage.containsKey(StorageKeys.DEVICE_ID_STORAGE_KEY)) {
            Credentials credentials = request.getCredentials();
            try {
                LoggedInEntity loggedInEntity =
                        apiClient.authScaToken(
                                credentials.getField(Key.USERNAME),
                                credentials.getField(Key.PASSWORD),
                                persistentStorage.get(StorageKeys.SCA_TOKEN_STORAGE_KEY),
                                deviceId);
                persistentStorage.put(
                        StorageKeys.SCA_TOKEN_STORAGE_KEY, loggedInEntity.getScaToken());
                return AuthenticationStepResponse.authenticationSucceeded();
            } catch (LoginException loginException) {
                persistentStorage.remove(StorageKeys.SCA_TOKEN_STORAGE_KEY);
                if (userAvailability.isUserAvailableForInteraction()) {
                    log.error("SCA ScaToken-> forcing manual auth");
                    return AuthenticationStepResponse.executeStepWithId("syncApp");
                }
                throw loginException;
            }

        } else return AuthenticationStepResponse.executeStepWithId("syncApp");
    }
}

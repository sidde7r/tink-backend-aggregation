package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class ScaTokenAuthenticationStep implements AuthenticationStep {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BecApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String deviceId;

    public ScaTokenAuthenticationStep(
            BecApiClient apiClient, PersistentStorage persistentStorage, String deviceId) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.deviceId = deviceId;
    }

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
                logger.error("SCA ScaToken-> forcing manual auth");
                persistentStorage.remove(StorageKeys.SCA_TOKEN_STORAGE_KEY);
                return AuthenticationStepResponse.executeStepWithId("syncApp");
            }

        } else return AuthenticationStepResponse.executeStepWithId("syncApp");
    }
}

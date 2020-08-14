package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import java.util.UUID;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.KeyCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.SecondFactorOperationsEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SingleSupplementalFieldAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class KeyCardAuthenticationStep implements AuthenticationStep {

    public static final String KEY_CARD_STEP_NAME = "keyCardStep";

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final BecApiClient apiClient;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = request.getCredentials();

        SecondFactorOperationsEntity secondFactorOperationsEntity =
                apiClient.postKeyCardPrepareAndDecryptResponse(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        getDeviceId());
        KeyCardEntity keyCardEntity = secondFactorOperationsEntity.getKeycard();
        sessionStorage.put(StorageKeys.KEY_CARD_NUMBER_STORAGE_KEY, keyCardEntity.getKeycardNo());
        sessionStorage.put(StorageKeys.CHALLENGE_STORAGE_KEY, keyCardEntity.getNemidChallenge());

        return AuthenticationStepResponse.executeStepWithId(
                SingleSupplementalFieldAuthenticationStep.class.getName());
    }

    @Override
    public String getIdentifier() {
        return KEY_CARD_STEP_NAME;
    }

    private String getDeviceId() {
        String deviceId = persistentStorage.get(StorageKeys.DEVICE_ID_STORAGE_KEY);
        if (deviceId != null) {
            return deviceId;
        } else {
            String macAddress = generateDeviceId();
            persistentStorage.put(StorageKeys.DEVICE_ID_STORAGE_KEY, macAddress);
            return macAddress;
        }
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

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
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class KeyCardAuthenticationStep implements AuthenticationStep {

    public static final String KEY_CARD_STEP_NAME = "keyCardStep";

    private final SessionStorage sessionStorage;
    private final BecApiClient apiClient;
    private final String deviceId;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Credentials credentials = request.getCredentials();

        SecondFactorOperationsEntity secondFactorOperationsEntity =
                apiClient.postKeyCardValuesAndDecryptResponse(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        deviceId);
        KeyCardEntity keyCardEntity = secondFactorOperationsEntity.getKeycard();
        sessionStorage.put(StorageKeys.KEY_CARD_NUMBER_STORAGE_KEY, keyCardEntity.getKeycardNo());
        sessionStorage.put(StorageKeys.CHALLENGE_STORAGE_KEY, keyCardEntity.getNemidChallenge());

        return AuthenticationStepResponse.executeStepWithId(
                SupplementalFieldsAuthenticationStep.class.getName());
    }

    @Override
    public String getIdentifier() {
        return KEY_CARD_STEP_NAME;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator;

import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CreditAgricolePinInputAuthStep extends SupplementalFieldsAuthenticationStep {

    private final PersistentStorage storage;

    CreditAgricolePinInputAuthStep(
            SupplementalInformationFormer supplementalInformationFormer,
            PersistentStorage storage) {
        super(
                CreditAgricolePinInputAuthStep.class.getName(),
                callbackData -> {
                    storage.put(
                            StorageKey.PROFILE_PIN, callbackData.get(Key.ACCESS_PIN.getFieldKey()));
                    return AuthenticationStepResponse.executeStepWithId(
                            CreditAgricoleAuthenticator.PROCESS_PROFILE_STEP);
                },
                supplementalInformationFormer.getField(Key.EMAIL),
                supplementalInformationFormer.getField(Key.ACCESS_PIN));
        this.storage = storage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        addValueToEmailField();
        return super.execute(request);
    }

    private void addValueToEmailField() {
        fields.stream()
                .filter(f -> f.getName().equals(Key.EMAIL.getFieldKey()))
                .findAny()
                .ifPresent(f -> f.setValue(storage.get(StorageKey.EMAIL)));
    }
}

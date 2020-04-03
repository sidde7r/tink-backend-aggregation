package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AxaLoginAuthenticationStep extends SupplementalFieldsAuthenticationStep {

    private final AxaStorage storage;

    public AxaLoginAuthenticationStep(
            SupplementalInformationFormer supplementalInformationFormer, AxaStorage storage) {
        super(
                AxaLoginAuthenticationStep.class.getName(),
                callbackData -> {
                    storage.storeCardReaderResponse(
                            callbackData.get(Key.LOGIN_INPUT.getFieldKey()));
                    return AuthenticationStepResponse.executeNextStep();
                },
                supplementalInformationFormer.getField(Key.LOGIN_DESCRIPTION),
                supplementalInformationFormer.getField(Key.LOGIN_INPUT));
        this.storage = storage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        addValueToField();
        return super.execute(request);
    }

    private void addValueToField() {
        fields.stream()
                .filter(f -> f.getName().equals(Key.LOGIN_DESCRIPTION.getFieldKey()))
                .findAny()
                .ifPresent(f -> f.setValue(storage.getOtpChallenge()));
    }
}

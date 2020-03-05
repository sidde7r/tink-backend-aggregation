package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AxaSigningAuthenticationStep extends SupplementalFieldsAuthenticationStep {

    private final AxaStorage storage;

    public AxaSigningAuthenticationStep(
            SupplementalInformationFormer supplementalInformationFormer, AxaStorage storage) {
        super(
                AxaSigningAuthenticationStep.class.getName(),
                callbackData -> {
                    storage.storeCardReaderResponse(
                            callbackData.get(Key.SIGN_CODE_INPUT.getFieldKey()));
                    return AuthenticationStepResponse.executeNextStep();
                },
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_DESCRIPTION),
                supplementalInformationFormer.getField(Field.Key.ADDITIONAL_INFORMATION),
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_INPUT));
        this.storage = storage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        addValueToField(Key.SIGN_CODE_DESCRIPTION, storage.getOtpChallenge());
        addValueToField(Key.ADDITIONAL_INFORMATION, storage.getCardNumberChallenge());
        return super.execute(request);
    }

    private void addValueToField(Field.Key fieldKey, String challengeCode) {
        fields.stream()
                .filter(f -> f.getName().equals(fieldKey.getFieldKey()))
                .findAny()
                .ifPresent(f -> f.setValue(challengeCode));
    }
}

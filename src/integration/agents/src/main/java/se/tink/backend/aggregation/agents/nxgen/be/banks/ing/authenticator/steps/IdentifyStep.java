package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorMultiData;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class IdentifyStep extends SupplementalFieldsAuthenticationStep {

    public IdentifyStep(
            IngConfiguration ingConfiguration,
            SupplementalInformationFormer supplementalInformationFormer) {
        super(
                "IDENTIFY",
                callback(ingConfiguration.getIngStorage()),
                supplementalInformationFormer.getField(Field.Key.OTP_INPUT));
    }

    private static CallbackProcessorMultiData callback(IngStorage ingStorage) {
        return callbackData -> {
            ingStorage.storeOtp(callbackData.get(Key.OTP_INPUT.getFieldKey()));
            return AuthenticationStepResponse.executeNextStep();
        };
    }
}

package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class OtpStep extends SingleSupplementalFieldAuthenticationStep {

    public OtpStep(
            final CallbackProcessorSingleData callbackProcessor,
            final SupplementalInformationFormer supplementalInformationFormer) {
        super(callbackProcessor, supplementalInformationFormer.getField(Key.OTP_INPUT));
    }

    public OtpStep(
            final CallbackProcessorSingleDataAndCredentials callbackProcessor,
            final SupplementalInformationFormer supplementalInformationFormer) {
        super(callbackProcessor, supplementalInformationFormer.getField(Key.OTP_INPUT));
    }
}

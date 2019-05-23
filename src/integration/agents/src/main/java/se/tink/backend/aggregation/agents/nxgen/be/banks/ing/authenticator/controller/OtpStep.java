package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public final class OtpStep {

    private final SupplementalInformationFormer supplementalInformationFormer;

    OtpStep(final SupplementalInformationFormer supplementalInformationFormer) {
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    public AuthenticationResponse respond() {
        List<Field> otpInput =
                Collections.singletonList(
                        supplementalInformationFormer.getField(Field.Key.OTP_INPUT));
        return new AuthenticationResponse(IngCardReaderAuthenticationController.STEP_OTP, otpInput);
    }
}

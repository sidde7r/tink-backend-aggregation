package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public final class OtpStep implements AuthenticationStep {

    private static Logger logger = LoggerFactory.getLogger(OtpStep.class);

    private final SupplementalInformationFormer supplementalInformationFormer;

    OtpStep(final SupplementalInformationFormer supplementalInformationFormer) {
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public AuthenticationStepResponse execute(final AuthenticationRequest request) {
        logger.info("ING OtpStep");

        List<Field> otpInput =
                Collections.singletonList(
                        supplementalInformationFormer.getField(Field.Key.OTP_INPUT));
        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder().withFields(otpInput).build());
    }
}

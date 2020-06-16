package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpInfoDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaOtpCodeExchanger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AuthorizeWithOtpStep extends SupplementalFieldsAuthenticationStep {

    static final String STEP_ID = "authorize_with_otp_step";

    private final AktiaOtpDataStorage otpDataStorage;

    public AuthorizeWithOtpStep(
            SupplementalInformationFormer supplementalInformationFormer,
            AktiaOtpCodeExchanger aktiaOtpCodeExchanger,
            AktiaOtpDataStorage otpDataStorage) {
        super(
                STEP_ID,
                callbackData -> {
                    final ExchangeOtpCodeStatus status =
                            aktiaOtpCodeExchanger.exchangeCode(
                                    callbackData.get(Field.Key.SIGN_CODE_INPUT.getFieldKey()));
                    otpDataStorage.storeStatus(status);

                    return AuthenticationStepResponse.executeNextStep();
                },
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_DESCRIPTION),
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_INPUT));

        this.otpDataStorage = otpDataStorage;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        fields.stream()
                .filter(f -> f.getName().equals(Field.Key.SIGN_CODE_DESCRIPTION.getFieldKey()))
                .findAny()
                .ifPresent(f -> f.setValue(getSignCodeFieldDescription()));

        return super.execute(request);
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }

    private String getSignCodeFieldDescription() {
        final OtpInfoDto otpInfoDto =
                otpDataStorage
                        .getInfo()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Otp Info not found in the storage."));

        return String.format(
                "Please provide one time password from card %s with index %s",
                otpInfoDto.getCurrentOtpCard(), otpInfoDto.getNextOtpIndex());
    }
}

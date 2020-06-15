package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class CheckOtpResponseStep implements AuthenticationStep {

    private final AktiaOtpDataStorage otpDataStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final ExchangeOtpCodeStatus status =
                otpDataStorage
                        .getStatus()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Exchange OTP code status not found in the storage."));

        if (status == ExchangeOtpCodeStatus.ACCOUNT_LOCKED) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        } else if (status == ExchangeOtpCodeStatus.OTHER_ERROR) {
            throw SessionError.SESSION_EXPIRED.exception();
        } else if (status == ExchangeOtpCodeStatus.WRONG_OTP_CODE) {
            return AuthenticationStepResponse.executeStepWithId(AuthorizeWithOtpStep.STEP_ID);
        }

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return "check_otp_response_step";
    }
}

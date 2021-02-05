package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.StepDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@RequiredArgsConstructor
public class SmsOtpStep extends AuthenticateBaseStep {

    public static final String STEP_ID = "smsOtpStep";

    private final BpceApiClient bpceApiClient;

    private final BpceStorage bpceStorage;

    private final SupplementalInformationHelper supplementalInformationHelper;

    private final BpceValidationHelper validationHelper;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        final String authTransactionPath = bpceStorage.getAuthTransactionPath();
        final StepDto credentialsResponse = bpceStorage.getCredentialsResponse();

        final String otpCode = supplementalInformationHelper.waitForOtpInput();

        final AuthTransactionResponseDto sendOtpResponse =
                sendOtp(authTransactionPath, credentialsResponse, otpCode);

        validateAuthenticationSucceeded(sendOtpResponse);

        bpceStorage.storeSamlPostAction(sendOtpResponse.getResponse().getSaml2Post());

        return AuthenticationStepResponse.executeStepWithId(AuthConsumeStep.STEP_ID);
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }

    private AuthTransactionResponseDto sendOtp(
            String authTransactionPath, StepDto credentialsResponse, String otpCode) {
        final String validationId = validationHelper.getValidationIdFromStep(credentialsResponse);
        final String validationUnitId =
                validationHelper.getValidationUnitIdFromStep(credentialsResponse, validationId);

        return bpceApiClient.sendOtp(validationId, validationUnitId, authTransactionPath, otpCode);
    }
}

package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication;

import java.util.Map;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorMultiData;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.SupplementalFieldsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class BPostBankSigningAuthenticationStep extends SupplementalFieldsAuthenticationStep {

    static final String STEP_ID = "BPost sign step";

    private final BPostBankApiClient apiClient;
    private final BPostBankAuthContext authContext;

    public BPostBankSigningAuthenticationStep(
            SupplementalInformationFormer supplementalInformationFormer,
            BPostBankApiClient apiClient,
            BPostBankAuthContext authContext) {
        super(
                BPostBankSigningAuthenticationStep.class.getName(),
                new SigningStepCallbackHandler(apiClient, authContext),
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_DESCRIPTION),
                supplementalInformationFormer.getField(Field.Key.SIGN_CODE_INPUT));
        this.apiClient = apiClient;
        this.authContext = authContext;
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        if (!authContext.isRegistrationInitialized()) {
            authContext.initRegistration(
                    apiClient.registrationInit(authContext), request.getCredentials());
            fields.stream()
                    .filter(f -> f.getName().equals(Field.Key.SIGN_CODE_DESCRIPTION.getFieldKey()))
                    .findAny()
                    .ifPresent(f -> f.setValue(getFormattedChallengeCode()));
        }
        return super.execute(request);
    }

    private String getFormattedChallengeCode() {
        StringBuilder sb =
                new StringBuilder(authContext.getChallengeCode().substring(0, 4))
                        .append(" ")
                        .append(authContext.getChallengeCode().substring(4));
        return sb.toString();
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }

    private static class SigningStepCallbackHandler implements CallbackProcessorMultiData {

        private final BPostBankApiClient apiClient;
        private final BPostBankAuthContext authContext;

        SigningStepCallbackHandler(BPostBankApiClient apiClient, BPostBankAuthContext authContext) {
            this.apiClient = apiClient;
            this.authContext = authContext;
        }

        @Override
        public AuthenticationStepResponse process(Map<String, String> callbackData)
                throws AuthenticationException, AuthorizationException {
            RegistrationResponseDTO responseDTO =
                    apiClient.registrationAuthorize(
                            authContext, callbackData.get(Field.Key.SIGN_CODE_INPUT.getFieldKey()));
            if (responseDTO.isErrorChallengeResponseIncorrect()) {
                return AuthenticationStepResponse.executeStepWithId(STEP_ID);
            } else if (responseDTO.isErrorAccountBlocked()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }
            return AuthenticationStepResponse.executeNextStep();
        }
    }
}

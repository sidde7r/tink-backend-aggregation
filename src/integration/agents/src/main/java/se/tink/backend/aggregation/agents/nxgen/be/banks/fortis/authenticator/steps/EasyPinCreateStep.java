package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.OtpInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.PhonenumberInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinCreateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class EasyPinCreateStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisRandomTokenGenerator fortisRandomTokenGenerator;
    private final FortisDataAccessorFactory fortisDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {

        String phoneNo = request.getUserInteractionData().getFieldValue(PhonenumberInputField.ID);
        phoneNo = phoneNo.replace(" ", "");

        request.getAuthenticationProcessState();

        FortisProcessStateAccessor processStateAccessor =
                fortisDataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());

        FortisProcessState processState = processStateAccessor.get();

        processState.setDeviceId(fortisRandomTokenGenerator.generateDeviceId());

        EasyPinCreateResponse easyPinCreateResponse =
                apiClient.easyPinCreate(phoneNo, processState.getDeviceId());

        if (easyPinCreateResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(easyPinCreateResponse),
                    request.getAuthenticationPersistedData());
        }

        processState.setOathTokenId(easyPinCreateResponse.getValue().getTokenId());
        processState.setRegistrationCode(easyPinCreateResponse.getValue().getRegistrationCode());
        processState.setEnrollmentSessionId(
                easyPinCreateResponse.getValue().getEnrollmentSessionId());

        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        EasyPinProvisionStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                processStateAccessor.store(processState),
                new OtpInputField());
    }
}

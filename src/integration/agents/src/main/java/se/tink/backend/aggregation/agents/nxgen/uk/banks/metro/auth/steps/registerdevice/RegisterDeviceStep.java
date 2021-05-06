package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice;

import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.AgentField;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.DeviceOperationRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.RegisterDeviceOperationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification.OtpVerificationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

@AllArgsConstructor
public class RegisterDeviceStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {
    private static final String SESSION_ID = "session_id";
    private static final String DEVICE_ID = "device_id";
    private static final String OTP_INPUT = "otpinput";

    private final MetroDataAccessorFactory metroDataAccessorFactory;

    private final RegisterDeviceCall registerDeviceCall;

    private final TulipReferenceCall tulipReferenceCall;

    private final RandomValueGenerator randomValueGenerator;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroProcessStateAccessor processStateAccessor =
                metroDataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());
        MetroProcessState processState = processStateAccessor.getProcessState();
        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();

        String generatedTulipReference = randomValueGenerator.getUUID().toString().replace("-", "");
        ExternalApiCallResult<String> tulipReference =
                tulipReferenceCall.execute(
                        generatedTulipReference,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        DeviceOperationRequest deviceOperationRequest =
                RequestFactory.create(
                        processState, authenticationData, tulipReference.getResponse().get());

        ExternalApiCallResult<RegisterDeviceOperationResponse> result =
                registerDeviceCall.execute(
                        deviceOperationRequest,
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        return result.getResponse()
                .map(
                        response ->
                                nextStep(
                                        persistedDataAccessor,
                                        processStateAccessor,
                                        processState,
                                        authenticationData,
                                        response))
                .orElseGet(
                        () ->
                                new AgentFailedAuthenticationResult(
                                        result.getAgentBankApiError().get(),
                                        request.getAuthenticationPersistedData()));
    }

    private AgentAuthenticationResult nextStep(
            MetroPersistedDataAccessor persistedDataAccessor,
            MetroProcessStateAccessor processStateAccessor,
            MetroProcessState processState,
            MetroAuthenticationData authenticationData,
            RegisterDeviceOperationResponse res) {
        AgentAuthenticationPersistedData authenticationPersistedData =
                persistedDataAccessor.storeAuthenticationData(
                        authenticationData.setDeviceId(res.getHeader(DEVICE_ID)));
        AgentAuthenticationProcessState storeProcessState =
                processStateAccessor.storeProcessState(
                        processState
                                .setSessionId(res.getHeader(SESSION_ID))
                                .setChallenge(res.getChallenge())
                                .setAssertionId(res.getAssertionId()));
        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        OtpVerificationStep.class.getSimpleName()),
                authenticationPersistedData,
                storeProcessState,
                new AgentField(Key.OTP_INPUT.getFieldKey(), OTP_INPUT));
    }
}

package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification;

import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.ActionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionData.OtpEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.MethodType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.OtpVerificationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.deviceregistration.DeviceRegistrationChallengeStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@AllArgsConstructor
public class OtpVerificationStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final MetroDataAccessorFactory metroDataAccessorFactory;

    private final OtpVerificationCall otpVerificationCall;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroProcessStateAccessor processStateAccessor =
                metroDataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());

        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();
        MetroProcessState processState = processStateAccessor.getProcessState();

        ConfirmChallengeRequest authenticationRequest =
                new ConfirmChallengeRequest(
                        new UserIdHeaderEntity(authenticationData.getUserId()),
                        AssertionEntity.builder()
                                .action(ActionType.AUTHENTICATION)
                                .assertionId(processState.getAssertionId())
                                .challenge(processState.getChallenge())
                                .otp(
                                        new OtpEntity(
                                                request.getUserInteractionData()
                                                        .getFieldValue(
                                                                Key.OTP_INPUT.getFieldKey())))
                                .method(MethodType.OTP)
                                .assertionType(AssertionType.AUTHENTICATE)
                                .build());

        ExternalApiCallResult<OtpVerificationResponse> result =
                otpVerificationCall.execute(
                        new OtpVerificationParameters(
                                processState.getSessionId(),
                                authenticationData.getDeviceId(),
                                authenticationData.getSigningKeyPair().getPrivate(),
                                authenticationRequest),
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        return result.getResponse()
                .map(
                        response ->
                                nextStep(
                                        persistedDataAccessor,
                                        processStateAccessor,
                                        authenticationData,
                                        processState,
                                        response))
                .orElseGet(
                        () ->
                                new AgentFailedAuthenticationResult(
                                        result.getAgentBankApiError().get(),
                                        persistedDataAccessor.storeAuthenticationData(
                                                authenticationData)));
    }

    private AgentAuthenticationResult nextStep(
            MetroPersistedDataAccessor persistedDataAccessor,
            MetroProcessStateAccessor processStateAccessor,
            MetroAuthenticationData authenticationData,
            MetroProcessState processState,
            OtpVerificationResponse res) {
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        DeviceRegistrationChallengeStep.class.getSimpleName()),
                processStateAccessor.storeProcessState(
                        processState.setAssertionId(res.getAssertionId())),
                persistedDataAccessor.storeAuthenticationData(authenticationData));
    }
}

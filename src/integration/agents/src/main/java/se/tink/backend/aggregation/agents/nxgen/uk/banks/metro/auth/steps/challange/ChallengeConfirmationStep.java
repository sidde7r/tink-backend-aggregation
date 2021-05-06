package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange;

import java.security.KeyPair;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.logindevice.LoginDeviceStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@AllArgsConstructor
public abstract class ChallengeConfirmationStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final MetroDataAccessorFactory metroDataAccessorFactory;

    private final ConfirmChallengeCall deviceRegistrationChallengeCall;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroProcessStateAccessor processStateAccessor =
                metroDataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());

        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();
        MetroProcessState processState = processStateAccessor.getProcessState();
        KeyPair signingKeys = authenticationData.getSigningKeyPair();
        ConfirmChallengeRequest requestBody = requestBody(authenticationData, processState);

        ExternalApiCallResult<ConfirmChallengeResponse> result =
                deviceRegistrationChallengeCall.execute(
                        new ConfirmChallengeParameters(
                                processState.getSessionId(),
                                authenticationData.getDeviceId(),
                                signingKeys.getPrivate(),
                                requestBody),
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
            ConfirmChallengeResponse res) {
        AgentAuthenticationProcessState authenticationProcessState =
                processStateAccessor.storeProcessState(processState.setToken(res.getToken()));
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(LoginDeviceStep.class.getSimpleName()),
                authenticationProcessState,
                persistedDataAccessor.storeAuthenticationData(authenticationData));
    }

    public abstract ConfirmChallengeRequest requestBody(
            MetroAuthenticationData authenticationData, MetroProcessState processState);
}

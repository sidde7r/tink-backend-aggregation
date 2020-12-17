package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.SecurityNumberSeedResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.registerdevice.RegisterDeviceStep;
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
@Slf4j
public class FetchSeedPositionStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final MetroDataAccessorFactory dataAccessorFactory;

    private final FetchSeedPositionCall fetchSeedPositionCall;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                dataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());

        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();

        ExternalApiCallResult<SecurityNumberSeedResponse> result =
                fetchSeedPositionCall.execute(
                        new FetchSeedPositionParameters(authenticationData.getUserId()),
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
            MetroAuthenticationData authenticationData,
            SecurityNumberSeedResponse res) {
        AgentAuthenticationProcessState processState =
                processStateAccessor.storeProcessState(
                        new MetroProcessState().setSeedPosition(res.indexPositions()));
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RegisterDeviceStep.class.getSimpleName()),
                processState,
                persistedDataAccessor.storeAuthenticationData(authenticationData));
    }
}

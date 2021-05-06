package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.savecredentials;

import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.fetchseedposition.FetchSeedPositionStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
public class CredentialsSaveStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final MetroDataAccessorFactory metroDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        AgentUserInteractionData userData = request.getUserInteractionData();
        MetroAuthenticationData authenticationData =
                persistedDataAccessor
                        .getAuthenticationData()
                        .setUserId(userData.getFieldValue(Key.USERNAME.getFieldKey()))
                        .setSecuredNumber(userData.getFieldValue(Key.SECURITY_NUMBER.getFieldKey()))
                        .setPassword(userData.getFieldValue(Key.PASSWORD.getFieldKey()));
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        FetchSeedPositionStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                persistedDataAccessor.storeAuthenticationData(authenticationData));
    }
}

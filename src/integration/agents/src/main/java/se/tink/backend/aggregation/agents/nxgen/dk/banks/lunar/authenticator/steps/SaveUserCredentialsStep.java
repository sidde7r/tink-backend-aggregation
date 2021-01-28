package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class SaveUserCredentialsStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final LunarDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {

        LunarAuthDataAccessor accessor =
                persistedDataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = accessor.get();

        AgentUserInteractionData userData = request.getUserInteractionData();

        String userId = userData.getFieldValue(Field.Key.USERNAME.getFieldKey());
        String password = userData.getFieldValue(Field.Key.PASSWORD.getFieldKey());
        String serviceCode = userData.getFieldValue(LunarConstants.Storage.ACCESS_PIN_INPUT_LABEL);

        authData.setUserId(userId);
        authData.setNemIdPassword(password);
        authData.setLunarPassword(serviceCode);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        GetNemIdTokenStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                accessor.storeData(authData));
    }
}

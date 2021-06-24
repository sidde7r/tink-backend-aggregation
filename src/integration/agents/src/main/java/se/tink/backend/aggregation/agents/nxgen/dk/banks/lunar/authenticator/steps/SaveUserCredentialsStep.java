package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@Slf4j
@RequiredArgsConstructor
public class SaveUserCredentialsStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final LunarDataAccessorFactory persistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {
        log.info("{} Entering SaveUserCredentialsStep", LUNAR_TAG);

        LunarAuthDataAccessor authDataAccessor =
                persistedDataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = authDataAccessor.get();

        AgentUserInteractionData userData = request.getUserInteractionData();

        String userId = userData.getFieldValue(Field.Key.USERNAME.getFieldKey());
        String password = userData.getFieldValue(Field.Key.PASSWORD.getFieldKey());

        authData.setUserId(userId);
        authData.setNemIdPassword(password);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(GetNemIdTokenStep.class),
                request.getAuthenticationProcessState(),
                authDataAccessor.storeData(authData));
    }
}

package se.tink.backend.aggregation.agents.agentplatform.authentication.result;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentplatform.authentication.UserInteractionService;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentRemoteInteractionAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentRemoteInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResultVisitor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAfterRemoteInteractionAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentRedirectAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentThirdPartyAppOpenAppAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

public class AgentAuthenticationResultAggregationHandler
        implements AgentAuthenticationResultVisitor {

    private final UserInteractionService userInteractionService;
    private final PersistentStorageService persistentStorageService;
    private AgentAuthenticationResultHandlingResult handlingResult;

    public AgentAuthenticationResultAggregationHandler(
            UserInteractionService userInteractionService,
            PersistentStorageService persistentStorageService) {
        this.userInteractionService = userInteractionService;
        this.persistentStorageService = persistentStorageService;
    }

    @Override
    public void visit(AgentRedirectAuthenticationResult arg) {
        handlingResult =
                userInteractionService
                        .redirect(arg.getRedirectUrl())
                        .map(
                                data ->
                                        AgentAuthenticationResultHandlingResult
                                                .nextAuthenticationRequest(
                                                        new AgentRemoteInteractionAuthenticationRequest(
                                                                arg.getAuthenticationProcessStepIdentifier()
                                                                        .get(),
                                                                arg
                                                                        .getAuthenticationPersistedData(),
                                                                arg.getAuthenticationProcessState()
                                                                        .orElse(
                                                                                new AgentAuthenticationProcessState(
                                                                                        new HashMap<>())),
                                                                new AgentRemoteInteractionData(
                                                                        data))))
                        .orElse(
                                AgentAuthenticationResultHandlingResult.authenticationFailed(
                                        new AgentPlatformAuthenticationProcessError(
                                                new NoUserInteractionResponseError())));
    }

    @Override
    public void visit(AgentThirdPartyAppOpenAppAuthenticationResult arg) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void visit(AgentUserInteractionDefinitionResult arg) {
        List<AgentFieldValue> values =
                userInteractionService.requestForFields(
                        arg.getUserInteractionDefinition().getRequiredFields());
        if (values.isEmpty()) {
            handlingResult =
                    AgentAuthenticationResultHandlingResult.authenticationFailed(
                            new AgentPlatformAuthenticationProcessError(
                                    new NoUserInteractionResponseError()));
        } else {

            handlingResult =
                    AgentAuthenticationResultHandlingResult.nextAuthenticationRequest(
                            new AgentUserInteractionAuthenticationProcessRequest(
                                    arg.getAuthenticationProcessStepIdentifier().get(),
                                    arg.getAuthenticationPersistedData(),
                                    arg.getAuthenticationProcessState()
                                            .orElse(
                                                    new AgentAuthenticationProcessState(
                                                            new HashMap<>())),
                                    values));
        }
    }

    @Override
    public void visit(AgentSucceededAuthenticationResult arg) {
        Optional.ofNullable(arg.getAuthenticationPersistedData())
                .ifPresent(data -> persistentStorageService.writeToAgentPersistentStorage(data));
        handlingResult = AgentAuthenticationResultHandlingResult.authenticationSuccess();
    }

    @Override
    public void visit(AgentFailedAuthenticationResult arg) {
        Optional.ofNullable(arg.getAuthenticationPersistedData())
                .ifPresent(data -> persistentStorageService.writeToAgentPersistentStorage(data));
        handlingResult =
                AgentAuthenticationResultHandlingResult.authenticationFailed(
                        new AgentPlatformAuthenticationProcessError(arg.getError()));
    }

    @Override
    public void visit(AgentProceedNextStepAuthenticationResult arg) {
        // no action is needed in case of automatic next step
    }

    @Override
    public void visit(AgentProceedNextStepAfterRemoteInteractionAuthenticationResult arg) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AgentAuthenticationResultHandlingResult handle(
            final AgentAuthenticationResult authenticationResult) {
        authenticationResult.accept(this);
        return handlingResult;
    }
}

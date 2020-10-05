package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.aggregation.agents.agentplatform.authentication.result.AgentAuthenticationResultAggregationHandler;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.AgentAuthenticationResultHandlingResult;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.AgentAuthenticationService;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;

public class AgentPlatformAuthenticationService {

    private final UserInteractionService userInteractionService;
    private final PersistentStorageService persistentStorageService;

    public AgentPlatformAuthenticationService(
            UserInteractionService userInteractionService,
            PersistentStorageService persistentStorageService) {
        this.userInteractionService = userInteractionService;
        this.persistentStorageService = persistentStorageService;
    }

    public void authenticate(final AgentPlatformAuthenticator agentPlatformAuthenticator) {
        AuthenticationExecutor executor =
                new AuthenticationExecutor(
                        persistentStorageService,
                        userInteractionService,
                        new AgentAuthenticationService(
                                agentPlatformAuthenticator.getAuthenticationProcess()));
        AgentAuthenticationResultHandlingResult handlingResult =
                executor.execute(
                        new AgentStartAuthenticationProcessRequest(
                                persistentStorageService.readFromAgentPersistentStorage()));
        while (handlingResult.getAgentAuthenticationNextRequest().isPresent()) {
            handlingResult =
                    executor.execute(handlingResult.getAgentAuthenticationNextRequest().get());
        }
        checkForAuthenticationError(handlingResult);
    }

    private void checkForAuthenticationError(
            AgentAuthenticationResultHandlingResult handlingResult) {
        if (handlingResult.getAuthenticationError().isPresent()) {
            throw handlingResult.getAuthenticationError().get().exception();
        }
    }

    private class AuthenticationExecutor {
        private final PersistentStorageService persistentStorageService;
        private final UserInteractionService userInteractionService;
        private final AgentAuthenticationService authenticationService;

        AuthenticationExecutor(
                PersistentStorageService persistentStorageService,
                UserInteractionService userInteractionService,
                AgentAuthenticationService authenticationService) {
            this.persistentStorageService = persistentStorageService;
            this.userInteractionService = userInteractionService;
            this.authenticationService = authenticationService;
        }

        AgentAuthenticationResultHandlingResult execute(
                AgentAuthenticationRequest authenticationRequest) {
            AgentAuthenticationResult authenticationResult =
                    authenticationService.authenticate(authenticationRequest);
            return new AgentAuthenticationResultAggregationHandler(
                            userInteractionService, persistentStorageService)
                    .handle(authenticationResult);
        }
    }
}

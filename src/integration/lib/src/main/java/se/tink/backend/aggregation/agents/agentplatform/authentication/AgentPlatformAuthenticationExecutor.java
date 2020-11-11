package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageServiceFactory;
import se.tink.backend.aggregation.nxgen.agents.AgentPersistentStorageReceiverAgentVisitor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentPlatformAuthenticationExecutor {

    public static void processAuthentication(
            Agent agent,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        AgentPersistentStorageReceiverAgentVisitor agentPersistentStorageReceiver =
                new AgentPersistentStorageReceiverAgentVisitor();
        agent.accept(agentPersistentStorageReceiver);

        PersistentStorageService persistentStorageService =
                PersistentStorageServiceFactory.create(
                        agent, agentPersistentStorageReceiver.getPersistentStorage().get());

        new AgentPlatformAuthenticationService(
                        new UserInteractionService(
                                supplementalInformationController, credentialsRequest),
                        persistentStorageService)
                .authenticate((AgentPlatformAuthenticator) agent);
    }
}

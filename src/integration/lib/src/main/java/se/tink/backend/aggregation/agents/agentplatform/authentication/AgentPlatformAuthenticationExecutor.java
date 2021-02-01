package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageServiceFactory;
import se.tink.backend.aggregation.nxgen.agents.LegacyAgentComponentProviderAgentVisitor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentPlatformAuthenticationExecutor {

    public void processAuthentication(
            Agent agent,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        LegacyAgentComponentProviderAgentVisitor legacyAgentComponentProvider =
                new LegacyAgentComponentProviderAgentVisitor();
        agent.accept(legacyAgentComponentProvider);

        PersistentStorageService persistentStorageService =
                PersistentStorageServiceFactory.create(
                        agent,
                        legacyAgentComponentProvider
                                .getPersistentStorage()
                                .orElse(new PersistentStorage()));
        AgentPlatformAuthenticator agentPlatformAuthenticator = (AgentPlatformAuthenticator) agent;
        new AgentPlatformAuthenticationService(
                        new UserInteractionService(
                                supplementalInformationController, credentialsRequest),
                        persistentStorageService,
                        credentialsRequest)
                .authenticate(agentPlatformAuthenticator);

        legacyAgentComponentProvider.doPostAuthenticationLegacyCleaning();
        if (agentPlatformAuthenticator.isBackgroundRefreshPossible()) {
            credentialsRequest.getCredentials().setType(CredentialsTypes.PASSWORD);
        }
    }
}

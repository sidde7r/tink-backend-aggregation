package se.tink.backend.aggregation.agents.framework.compositeagenttest.command;

import com.google.inject.Inject;
import org.junit.Assert;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.executor.ProgressiveLoginExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class LoginCommand implements CompositeAgentTestCommand {

    private final Agent agent;
    private final CredentialsRequest credentialsRequest;
    private final SupplementalInformationController supplementalInformationController;

    @Inject
    private LoginCommand(
            Agent agent,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        this.agent = agent;
        this.credentialsRequest = credentialsRequest;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public void execute() throws Exception {
        if (isLoggedIn(agent)) {
            return;
        }

        if (agent instanceof AgentPlatformAuthenticator) {
            AgentPlatformAuthenticationExecutor.processAuthentication(
                    agent, credentialsRequest, supplementalInformationController);
            return;
        } else if (agent instanceof ProgressiveAuthAgent) {
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, (ProgressiveAuthAgent) agent);
            executor.login(credentialsRequest);
            return;
        }

        boolean loginSuccessful = agent.login();
        Assert.assertTrue("Agent could not login successfully.", loginSuccessful);
    }

    private boolean isLoggedIn(Agent agent) throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return false;
        }

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        persistentAgent.loadLoginSession();
        if (!persistentAgent.isLoggedIn()) {
            persistentAgent.clearLoginSession();
            return false;
        }
        return true;
    }
}

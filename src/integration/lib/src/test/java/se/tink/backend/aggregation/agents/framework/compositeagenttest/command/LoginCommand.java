package se.tink.backend.aggregation.agents.framework.compositeagenttest.command;

import com.google.inject.Inject;
import org.junit.Assert;
import se.tink.agent.compatibility_layers.aggregation_service.authentication.Authentication;
import se.tink.agent.compatibility_layers.aggregation_service.authentication.report.AuthenticationReport;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
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

    private final AgentInstance agentInstance;
    private final CredentialsRequest credentialsRequest;
    private final SupplementalInformationController supplementalInformationController;

    @Inject
    private LoginCommand(
            AgentInstance agentInstance,
            CredentialsRequest credentialsRequest,
            SupplementalInformationController supplementalInformationController) {
        this.agentInstance = agentInstance;
        this.credentialsRequest = credentialsRequest;
        this.supplementalInformationController = supplementalInformationController;
    }

    @Override
    public void execute() throws Exception {
        if (agentInstance.supportsAuthentication()) {
            authenticateAgentSdk();
        } else {
            authenticateAgent();
        }
    }

    private void authenticateAgentSdk() {
        Authentication authentication =
                new Authentication(supplementalInformationController, agentInstance);

        AuthenticationReport authenticationReport =
                authentication.initiate(credentialsRequest.isForceAuthenticate());

        Assert.assertEquals(
                "Agent could not login successfully.",
                ConsentStatus.VALID,
                authenticationReport.getConsentStatus());
    }

    private void authenticateAgent() throws Exception {
        Agent agent =
                agentInstance
                        .instanceOf(Agent.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Agent does not implement Agent.class."));

        if (isLoggedIn(agent)) {
            return;
        }

        if (agent instanceof AgentPlatformAuthenticator) {
            new AgentPlatformAuthenticationExecutor()
                    .processAuthentication(
                            agent, credentialsRequest, supplementalInformationController);
        } else if (agent instanceof ProgressiveAuthAgent) {
            final ProgressiveLoginExecutor executor =
                    new ProgressiveLoginExecutor(
                            supplementalInformationController, (ProgressiveAuthAgent) agent);
            executor.login(credentialsRequest);
        } else {
            boolean loginSuccessful = agent.login();
            Assert.assertTrue("Agent could not login successfully.", loginSuccessful);
        }
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

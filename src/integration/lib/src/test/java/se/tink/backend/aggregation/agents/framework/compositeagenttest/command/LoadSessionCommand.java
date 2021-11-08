package se.tink.backend.aggregation.agents.framework.compositeagenttest.command;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;

public class LoadSessionCommand implements CompositeAgentTestCommand {
    private final Agent agent;

    @Inject
    public LoadSessionCommand(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void execute() throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return;
        }

        // `loadLoginSession()` must be invoked for the agent to load the persistent & session
        // storage from the Credentials. This is done as part of Authentication::isLoggedIn(), but
        // must be done explicitly like this in case the agent test skip authentication.
        PersistentLogin persistentAgent = (PersistentLogin) agent;
        persistentAgent.loadLoginSession();
    }
}

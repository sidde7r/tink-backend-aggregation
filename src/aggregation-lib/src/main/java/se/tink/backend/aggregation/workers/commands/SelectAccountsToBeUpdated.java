package se.tink.backend.aggregation.workers.commands;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.SetAccountsToAggregateContext;

public class SelectAccountsToBeUpdated extends AgentWorkerCommand {
    private static final Logger log = LoggerFactory.getLogger(RequestUserOptInAccountsAgentWorkerCommand.class);
    private final SetAccountsToAggregateContext context;

    public SelectAccountsToBeUpdated(SetAccountsToAggregateContext context) {
        this.context = context;
    }

    // refresh account and send supplemental information to system
    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        context.setAccounts(Lists.newArrayList()); // FIXME
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {

    }

}

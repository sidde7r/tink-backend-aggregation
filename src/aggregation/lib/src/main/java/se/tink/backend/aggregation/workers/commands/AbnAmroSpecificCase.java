package se.tink.backend.aggregation.workers.commands;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

/**
 * This is a special command that will only do something for nl-abnamro provider. This provider is a
 * special provider that is only used by ABN Amro themselves to authenticate the user in GRIP app,
 * using ABN Amro's Internet Banking app. Not having this command causes us to throw away
 * Transactions in the ABN Amro-specific (legacy) Connector. Once ABN Amro has migrated away from
 * the old Connector, and use the generic one, this command can be removed. See this thread for more
 * information: https://tink.slack.com/archives/CB12SB8DV/p1588672355268300
 *
 * <p>This command needs to be after SendAccountsToUpdateServiceAgentWorkerCommand as
 * SendAccountsToUpdateServiceAgentWorkerCommand has side effects on the AgentContext The result of
 * this command did exist previously in the AbnAmroAgent but was accidentally remove in commit
 * 04a559c27a4731c117372228eda95dcf211f1024. Now it is not possible to put it in the Agent anymore
 * as the flow looks different now and as stated before, this needs to happen after accounts have
 * been send to UpdateService.
 *
 * <p>Johannes Elgh - 2020-05-07
 */
public class AbnAmroSpecificCase extends AgentWorkerCommand {
    private static class InternalAccountPayloadKeys {
        static final String SUBSCRIBED = "subscribed";
    }

    private AgentWorkerCommandContext context;

    public AbnAmroSpecificCase(AgentWorkerCommandContext context) {
        this.context = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {

        List<Account> updatedAccounts = context.getUpdatedAccounts();
        if (updatedAccounts != null) {
            for (Account account : updatedAccounts) {
                if (isSubscribed(account)) {
                    context.setWaitingOnConnectorTransactions(true);
                    break;
                }
            }
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    private static boolean isSubscribed(Account account) {
        String subscribed = account.getPayload(InternalAccountPayloadKeys.SUBSCRIBED);
        return Boolean.parseBoolean(subscribed);
    }

    @Override
    protected void doPostProcess() throws Exception {}
}

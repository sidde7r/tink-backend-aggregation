package se.tink.backend.aggregation.workers.commands;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.account_data_cache.FilterReason;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SendAccountRestrictionEventsWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountRestrictionEventsWorkerCommand.class);
    private final AgentWorkerCommandContext context;
    private final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;

    public SendAccountRestrictionEventsWorkerCommand(
            AgentWorkerCommandContext context,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {
        this.context = context;
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            log.info("Executing SendAccountRestrictionEventsWorkerCommand");
            log.info(
                    " FilteredOutAccountData - size: {}",
                    context.getAccountDataCache()
                            .getFilteredOutAccountDataWithFilterReason()
                            .size());
            context.getAccountDataCache()
                    .getFilteredOutAccountDataWithFilterReason()
                    .forEach(
                            filteredOutAccountData -> {
                                Account filteredOutAccount =
                                        filteredOutAccountData.getLeft().getAccount();
                                List<FilterReason> filterReasons =
                                        filteredOutAccountData.getRight();
                                filterReasons.forEach(
                                        reason ->
                                                sendAccountAggregationRestrictedEvent(
                                                        filteredOutAccount, reason));
                            });
        } catch (Exception e) {
            log.warn("Could not execute SendAccountRestrictionEventsWorkerCommand", e);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private void sendAccountAggregationRestrictedEvent(
            Account restrictedAccount, FilterReason filterReason) {
        log.info(
                "Preparing to send AccountAggregationRestrictedEvent for {}",
                restrictedAccount.getId());
        CredentialsRequest request = context.getRequest();
        if (request.getProvider() == null) {
            return;
        }
        accountInformationServiceEventsProducer.sendAccountAggregationRestrictedEvent(
                context.getClusterId(),
                context.getAppId(),
                request.getUser().getId(),
                request.getProvider(),
                context.getCorrelationId(),
                request.getCredentials().getId(),
                restrictedAccount.getId(),
                restrictedAccount.getType().name(),
                filterReason.name());
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Intentionally left empty.
    }
}

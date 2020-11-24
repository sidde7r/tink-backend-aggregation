package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SendAccountSourceInfoEventWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountSourceInfoEventWorkerCommand.class);
    private final AgentWorkerCommandContext context;
    private final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;

    public SendAccountSourceInfoEventWorkerCommand(
            AgentWorkerCommandContext context,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {
        this.context = context;
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            this.context.getAccountDataCache().getAllAccounts().forEach(this::sendSourceInfoEvent);

        } catch (RuntimeException e) {
            log.warn("Could not execute SendAccountSourceInfoEventWorkerCommand", e);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private void sendSourceInfoEvent(Account account) {
        CredentialsRequest request = context.getRequest();
        AccountSourceInfo accountSourceInfo = account.getSourceInfo();
        if (Objects.isNull(accountSourceInfo.getBankAccountType())
                && Objects.isNull(accountSourceInfo.getBankProductCode())
                && Objects.isNull(accountSourceInfo.getBankProductName())) {
            return; // don't send source info if there is none
        }
        if (request.getProvider() == null) {
            return;
        }
        accountInformationServiceEventsProducer.sendAccountSourceInfoEvent(
                context.getClusterId(),
                context.getAppId(),
                request.getUser().getId(),
                request.getProvider(),
                context.getCorrelationId(),
                request.getCredentials().getId(),
                account.getId(),
                accountSourceInfo);
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Intentionally left empty.
    }
}

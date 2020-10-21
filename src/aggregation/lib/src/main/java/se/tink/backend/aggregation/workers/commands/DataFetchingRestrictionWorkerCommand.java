package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;

public class DataFetchingRestrictionWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(DataFetchingRestrictionWorkerCommand.class);
    private final AgentWorkerCommandContext context;
    private final ControllerWrapper controllerWrapper;

    public DataFetchingRestrictionWorkerCommand(
            AgentWorkerCommandContext context, ControllerWrapper controllerWrapper) {
        this.context = context;
        this.controllerWrapper = controllerWrapper;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            Credentials credentials = context.getRequest().getCredentials();
            List<DataFetchingRestrictions> dfRestrictions =
                    context.getRequest().getDataFetchingRestrictions();
            if (dfRestrictions.isEmpty()) {
                return AgentWorkerCommandResult.CONTINUE;
            }

            List<AccountTypes> restrictedAccountTypes =
                    dfRestrictions.stream()
                            .map(this::mapToAccountType)
                            .collect(Collectors.toList());
            if (hasCheckingRestriction(dfRestrictions)) {
                restrictedAccountTypes.add(AccountTypes.OTHER);
            }

            log.info(
                    "Sending Restrict Accounts under credentialsId: {}, for the following account types: {}",
                    credentials.getId(),
                    restrictedAccountTypes);

            controllerWrapper.restrictAccounts(
                    new RestrictAccountsRequest()
                            .setUserId(credentials.getUserId())
                            .setCredentialsId(credentials.getId())
                            .setAccountTypes(restrictedAccountTypes));
        } catch (RuntimeException e) {
            // don't fail refresh if sending information about restricted accounts failed
            log.warn("Execution of DataFetchingRestrictionWorkerCommand failed", e);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private boolean hasCheckingRestriction(List<DataFetchingRestrictions> dfRestrictions) {
        return dfRestrictions.stream()
                .anyMatch(
                        restriction ->
                                restriction.equals(
                                        DataFetchingRestrictions
                                                .RESTRICT_FETCHING_CHECKING_ACCOUNTS));
    }

    private AccountTypes mapToAccountType(DataFetchingRestrictions dataFetchingRestrictions) {
        final Map<DataFetchingRestrictions, AccountTypes> map =
                ImmutableMap.<DataFetchingRestrictions, AccountTypes>builder()
                        .put(
                                DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS,
                                AccountTypes.CHECKING)
                        .put(
                                DataFetchingRestrictions.RESTRICT_FETCHING_SAVINGS_ACCOUNTS,
                                AccountTypes.SAVINGS)
                        .put(
                                DataFetchingRestrictions.RESTRICT_FETCHING_CREDIT_CARD_ACCOUNTS,
                                AccountTypes.CREDIT_CARD)
                        .put(
                                DataFetchingRestrictions.RESTRICT_FETCHING_INVESTMENT_ACCOUNTS,
                                AccountTypes.INVESTMENT)
                        .put(
                                DataFetchingRestrictions.RESTRICT_FETCHING_LOAN_ACCOUNTS,
                                AccountTypes.LOAN)
                        .build();
        return map.get(dataFetchingRestrictions);
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Intentionally left empty.
    }
}

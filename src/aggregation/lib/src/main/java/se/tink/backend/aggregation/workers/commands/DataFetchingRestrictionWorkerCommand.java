package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;

public class DataFetchingRestrictionWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(DataFetchingRestrictionWorkerCommand.class);
    private final AgentWorkerCommandContext context;
    private final ControllerWrapper controllerWrapper;
    private final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;

    public DataFetchingRestrictionWorkerCommand(
            AgentWorkerCommandContext context,
            ControllerWrapper controllerWrapper,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {
        this.context = context;
        this.controllerWrapper = controllerWrapper;
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            List<DataFetchingRestrictions> dfRestrictions =
                    context.getRequest().getDataFetchingRestrictions();
            if (dfRestrictions.isEmpty()) {
                return AgentWorkerCommandResult.CONTINUE;
            }

            List<AccountTypes> restrictedAccountTypes = getRestrictedAccountTypes(dfRestrictions);
            if (allowRefreshRegardlessOfRestrictions()) {
                List<Account> restrictedAccounts = new ArrayList<>();
                this.context
                        .getAccountDataCache()
                        .getFilteredAccountData()
                        .forEach(
                                accountData -> { // we can't register the filter yet as we need to
                                    // figure out which accounts have been restricted
                                    Account account = accountData.getAccount();
                                    if (restrictedAccountTypes.contains(account.getType())) {
                                        restrictedAccounts.add(account);
                                    }
                                });

                registerAccountFilter(restrictedAccounts, restrictedAccountTypes);
                sendEvents(restrictedAccounts);
            }
            sendRestrictAccounts(restrictedAccountTypes);

        } catch (RuntimeException e) {
            log.warn("Could not execute DataFetchingRestrictionWorkerCommand", e);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private void sendEvents(List<Account> restrictedAccounts) {
        sendAccountAggregationRestrictedEvents(restrictedAccounts);
    }

    private void sendAccountAggregationRestrictedEvents(List<Account> restrictedAccounts) {
        CredentialsRequest request = context.getRequest();
        if (request.getProvider() == null) {
            return;
        }
        restrictedAccounts.forEach(
                restrictedAccount ->
                        accountInformationServiceEventsProducer
                                .sendAccountAggregationRestrictedEvent(
                                        context.getClusterId(),
                                        context.getAppId(),
                                        request.getUser().getId(),
                                        request.getProvider(),
                                        context.getCorrelationId(),
                                        request.getCredentials().getId(),
                                        restrictedAccount.getId(),
                                        restrictedAccount.getType().name()));
    }

    private boolean allowRefreshRegardlessOfRestrictions() {
        return context.getAgentsServiceConfiguration()
                .isFeatureEnabled("allowRefreshRegardlessOfRestrictions");
    }

    private void registerAccountFilter(
            List<Account> restrictedAccounts, List<AccountTypes> restrictedAccountTypes) {
        if (restrictedAccounts.isEmpty()) {
            return;
        }
        log.info(
                "Applying account restriction filter for credentialsId: {}, restricted AccountTypes: {}",
                context.getRequest().getCredentials().getId(),
                restrictedAccountTypes);
        this.context
                .getAccountDataCache()
                .addFilter(account -> !restrictedAccountTypes.contains(account.getType()));
    }

    private void sendRestrictAccounts(List<AccountTypes> restrictedAccountTypes) {
        Credentials credentials = context.getRequest().getCredentials();
        log.info(
                "Sending Restrict Accounts under credentialsId: {}, for the following account types: {}",
                credentials.getId(),
                restrictedAccountTypes);

        controllerWrapper.restrictAccounts(
                new RestrictAccountsRequest()
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setAccountTypes(restrictedAccountTypes));
    }

    private List<AccountTypes> getRestrictedAccountTypes(
            List<DataFetchingRestrictions> dfRestrictions) {
        List<AccountTypes> restrictedAccountTypes =
                dfRestrictions.stream()
                        .map(this::mapToAccountType)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        if (hasCheckingRestriction(dfRestrictions)) {
            restrictedAccountTypes.add(AccountTypes.OTHER);
        }
        return restrictedAccountTypes;
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

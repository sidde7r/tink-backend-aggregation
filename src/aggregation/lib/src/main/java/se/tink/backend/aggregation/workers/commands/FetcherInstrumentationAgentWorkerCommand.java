package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.utils.typeguesser.accountholder.AccountHolderTypeUtil;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class FetcherInstrumentationAgentWorkerCommand extends AgentWorkerCommand {
    private static final Logger log =
            LoggerFactory.getLogger(FetcherInstrumentationAgentWorkerCommand.class);
    public static final MetricId AGENT_FETCHER_INSTRUMENTATION_FILTERED_AWAY =
            MetricId.newId("agent_fetcher_instrumentation_filtered_away_accounts");

    private final AgentWorkerCommandContext context;
    private final Provider provider;
    private final Set<RefreshableItem> requestedItems;
    private MetricRegistry metricRegistry;

    public FetcherInstrumentationAgentWorkerCommand(
            AgentWorkerCommandContext context, Set<RefreshableItem> requestedItems) {
        this.context = context;
        this.requestedItems = requestedItems;
        this.provider = context.getRequest().getProvider();
        this.metricRegistry = context.getMetricRegistry();
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        Agent agent = context.getAgent();
        if (agent instanceof NextGenerationAgent) {
            NextGenerationAgent nxGenAgent = (NextGenerationAgent) agent;

            // Default implementation of NextGenerationAgent returns empty
            // Agents may override the method to get deeper instrumentation
            // on what account types was seen at the bank.
            Optional<FetcherInstrumentationRegistry> instrumentation =
                    nxGenAgent.getFetcherInstrumentation();
            if (instrumentation.isPresent()) {
                trackFilteredAccounts(instrumentation.get(), requestedItems);
                // here other tracking can be added
            }
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    private void trackFilteredAccounts(
            FetcherInstrumentationRegistry instrumentation, Set<RefreshableItem> requestedItems) {
        AccountHolderType requestedHolderType = AccountHolderTypeUtil.inferHolderType(provider);

        // From all HolderAccountType:s, filter out those that are not asked for
        Set<AccountHolderType> nonRequestedAccountHolderTypes =
                Arrays.stream(AccountHolderType.values())
                        .filter(ht -> ht != requestedHolderType)
                        .collect(Collectors.toSet());

        // From all requested RefreshableItems, convert them to their corresponding AccountTypes
        Set<AccountTypes> requestedAccountTypes =
                requestedItems.stream()
                        .flatMap(i -> convertItemToAccountTypes(i).stream())
                        .collect(Collectors.toSet());

        // From all possible AccountTypes, fifilter out those that are not asked for
        Set<AccountTypes> nonRequestedAccountTypes =
                Arrays.stream(AccountTypes.values())
                        .filter(t -> !requestedAccountTypes.contains(t))
                        .collect(Collectors.toSet());

        // For the requested holder type, loop over all nonRequestAccountTypes
        for (AccountTypes type : nonRequestedAccountTypes) {
            int numberAccountsSeen =
                    instrumentation.getNumberAccountsSeen(requestedHolderType, type);
            MetricId metricId =
                    createMetricId(provider, requestedHolderType, type, numberAccountsSeen);
            metricRegistry.meter(metricId).inc();
        }

        // For the non-requested holder types, loop over all account types
        for (AccountHolderType holderType : nonRequestedAccountHolderTypes) {
            for (AccountTypes type : AccountTypes.values()) {
                int numberAccountsSeen = instrumentation.getNumberAccountsSeen(holderType, type);
                MetricId metricId = createMetricId(provider, holderType, type, numberAccountsSeen);
                metricRegistry.meter(metricId).inc();
            }
        }
    }

    @Override
    protected void doPostProcess() throws Exception {
        // no-op
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add(
                                "class",
                                FetcherInstrumentationAgentWorkerCommand.class.getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }

    private static String numberAccounts(int num) {
        return num >= 10 ? "10+" : String.valueOf(num);
    }

    private static MetricId createMetricId(
            Provider provider, AccountHolderType holderType, AccountTypes type, int num) {
        return AGENT_FETCHER_INSTRUMENTATION_FILTERED_AWAY
                .label("market", provider.getMarket())
                .label("provider", provider.getName())
                .label("holder_type", holderType.name())
                .label("account_type", type.name())
                .label("filtered_away", numberAccounts(num));
    }

    private static Set<AccountTypes> convertItemToAccountTypes(RefreshableItem item) {
        switch (item) {
            case CHECKING_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.CHECKING);
            case LOAN_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.LOAN, AccountTypes.MORTGAGE);
            case SAVING_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.SAVINGS);
            case CREDITCARD_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.CREDIT_CARD);
            case INVESTMENT_ACCOUNTS:
                return ImmutableSet.of(AccountTypes.INVESTMENT, AccountTypes.PENSION);
            default:
                log.warn("Not implemented!");
                return ImmutableSet.of();
        }
    }
}

package se.tink.backend.aggregation.agents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshSummary;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshableItemFetchingStatus;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.rpc.Transfer;

@Slf4j
public final class RefreshExecutorUtils {

    private RefreshExecutorUtils() {
        throw new AssertionError();
    }

    private static final Map<RefreshableItem, Class> REFRESHABLEITEM_EXECUTOR_MAP =
            ImmutableMap.<RefreshableItem, Class>builder()
                    .put(RefreshableItem.EINVOICES, RefreshEInvoiceExecutor.class)
                    .put(
                            RefreshableItem.TRANSFER_DESTINATIONS,
                            RefreshTransferDestinationExecutor.class)
                    .put(RefreshableItem.CHECKING_ACCOUNTS, RefreshCheckingAccountsExecutor.class)
                    .put(
                            RefreshableItem.CHECKING_TRANSACTIONS,
                            RefreshCheckingAccountsExecutor.class)
                    .put(RefreshableItem.SAVING_ACCOUNTS, RefreshSavingsAccountsExecutor.class)
                    .put(RefreshableItem.SAVING_TRANSACTIONS, RefreshSavingsAccountsExecutor.class)
                    .put(
                            RefreshableItem.CREDITCARD_ACCOUNTS,
                            RefreshCreditCardAccountsExecutor.class)
                    .put(
                            RefreshableItem.CREDITCARD_TRANSACTIONS,
                            RefreshCreditCardAccountsExecutor.class)
                    .put(RefreshableItem.IDENTITY_DATA, RefreshIdentityDataExecutor.class)
                    .put(RefreshableItem.LOAN_ACCOUNTS, RefreshLoanAccountsExecutor.class)
                    .put(RefreshableItem.LOAN_TRANSACTIONS, RefreshLoanAccountsExecutor.class)
                    .put(
                            RefreshableItem.INVESTMENT_ACCOUNTS,
                            RefreshInvestmentAccountsExecutor.class)
                    .put(
                            RefreshableItem.INVESTMENT_TRANSACTIONS,
                            RefreshInvestmentAccountsExecutor.class)
                    .put(RefreshableItem.LIST_BENEFICIARIES, RefreshBeneficiariesExecutor.class)
                    .build();

    private static Class getRefreshExecutor(RefreshableItem item) {
        return REFRESHABLEITEM_EXECUTOR_MAP.get(item);
    }

    /**
     * Executes refresh on requested items. Under the hood agent calls the requested Bank API for
     * specific item. After the refresh context is updated. All refreshes that touches accounts are
     * cached, hence they are available for Opt-in feature.
     *
     * @param agent - Agent class
     * @param item - what should be refreshed
     * @param context - additional data from agent context
     * @return if refresh was fully successful
     */
    public static boolean executeSegregatedRefresher(
            Agent agent, RefreshableItem item, AgentContext context) {
        Class executorKlass = RefreshExecutorUtils.getRefreshExecutor(item);
        if (executorKlass == null) {
            throw new NotImplementedException(
                    String.format("No implementation for %s", item.name()));
        }
        // Segregated refresh executor
        if (executorKlass.isAssignableFrom(agent.getAgentClass())) {
            switch (item) {
                case CHECKING_ACCOUNTS:
                    refreshAndCacheCheckingAccounts(agent, context);
                    break;
                case CHECKING_TRANSACTIONS:
                    return fetchCheckingTransactions(
                            (RefreshCheckingAccountsExecutor) agent, context);
                case SAVING_ACCOUNTS:
                    refreshAndCacheSavingAccounts((RefreshSavingsAccountsExecutor) agent, context);
                    break;
                case SAVING_TRANSACTIONS:
                    return fetchSavingTransactions((RefreshSavingsAccountsExecutor) agent, context);
                case CREDITCARD_ACCOUNTS:
                    refreshAndCacheCreditCardAccounts(
                            (RefreshCreditCardAccountsExecutor) agent, context);
                    break;
                case CREDITCARD_TRANSACTIONS:
                    return fetchCreditCardTransactions(
                            (RefreshCreditCardAccountsExecutor) agent, context);
                case LOAN_ACCOUNTS:
                    refreshAndCacheLoanAccounts((RefreshLoanAccountsExecutor) agent, context);
                    break;
                case LOAN_TRANSACTIONS:
                    return fetchLoansTransactions((RefreshLoanAccountsExecutor) agent, context);
                case INVESTMENT_ACCOUNTS:
                    refreshAndCacheInvestmentAccounts(
                            (RefreshInvestmentAccountsExecutor) agent, context);
                    break;
                case INVESTMENT_TRANSACTIONS:
                    return fetchInvestmentTransactions(
                            (RefreshInvestmentAccountsExecutor) agent, context);
                case IDENTITY_DATA:
                    refreshIdentityData((RefreshIdentityDataExecutor) agent, context);
                    break;
                case EINVOICES:
                    refreshEInvoices((RefreshEInvoiceExecutor) agent, context);
                    break;
                case TRANSFER_DESTINATIONS:
                    refreshTransferDestinations(
                            (RefreshTransferDestinationExecutor) agent, context);
                    break;
                case LIST_BENEFICIARIES:
                    refreshListBeneficiaries((RefreshBeneficiariesExecutor) agent, context);
                    break;

                default:
                    throw new IllegalStateException(
                            String.format("Invalid refreshable item detected %s", item.name()));
            }
        } else {
            log.warn(
                    "[Refresh Executor Utils] A request for {} is received, agent is not capable of doing this",
                    item.name());
        }
        return true;
    }

    private static void refreshAndCacheCheckingAccounts(Agent agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching checking accounts.");
        summary.addItemSummary(RefreshableItem.CHECKING_ACCOUNTS);
        List<Account> checkingAccounts =
                ((RefreshCheckingAccountsExecutor) agent).fetchCheckingAccounts().getAccounts();

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching checking accounts, size: {}",
                checkingAccounts.size());
        summary.updateItemSummary(
                RefreshableItem.CHECKING_ACCOUNTS,
                RefreshableItemFetchingStatus.COMPLETED,
                checkingAccounts.size());

        logIfExtraAccounts(agent, checkingAccounts);
        context.cacheAccounts(checkingAccounts);
    }

    private static boolean fetchCheckingTransactions(
            RefreshCheckingAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        try {
            log.info("[Refresh Executor Utils] Start fetching checking transactions.");
            summary.addItemSummary(RefreshableItem.CHECKING_TRANSACTIONS);
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchCheckingTransactions().getTransactions().entrySet();

            List<Integer> fetchedTransactionsCounters =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching checking transactions, size: {}",
                    fetchedTransactionsCounters);
            summary.updateItemSummary(
                    RefreshableItem.CHECKING_TRANSACTIONS,
                    RefreshableItemFetchingStatus.COMPLETED,
                    fetchedTransactionsCounters);
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some checking account transactions.",
                    e);
            summary.updateItemSummary(
                    RefreshableItem.CHECKING_TRANSACTIONS,
                    RefreshableItemFetchingStatus.INTERRUPTED);
            return false;
        }
    }

    private static void refreshAndCacheSavingAccounts(
            RefreshSavingsAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching saving accounts.");
        summary.addItemSummary(RefreshableItem.SAVING_ACCOUNTS);
        List<Account> savingAccounts = agent.fetchSavingsAccounts().getAccounts();

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching saving accounts, size: {}",
                savingAccounts.size());
        summary.updateItemSummary(
                RefreshableItem.SAVING_ACCOUNTS,
                RefreshableItemFetchingStatus.COMPLETED,
                savingAccounts.size());

        context.cacheAccounts(savingAccounts);
    }

    private static boolean fetchSavingTransactions(
            RefreshSavingsAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        try {
            log.info("[Refresh Executor Utils] Start fetching saving transactions.");
            summary.addItemSummary(RefreshableItem.SAVING_TRANSACTIONS);
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchSavingsTransactions().getTransactions().entrySet();

            List<Integer> fetchedTransactionsCounters =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching saving transactions, size: {}",
                    fetchedTransactionsCounters);
            summary.updateItemSummary(
                    RefreshableItem.SAVING_TRANSACTIONS,
                    RefreshableItemFetchingStatus.COMPLETED,
                    fetchedTransactionsCounters);
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some saving account transactions.",
                    e);
            summary.updateItemSummary(
                    RefreshableItem.SAVING_TRANSACTIONS, RefreshableItemFetchingStatus.INTERRUPTED);
            return false;
        }
    }

    private static void refreshAndCacheCreditCardAccounts(
            RefreshCreditCardAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching credit card accounts.");
        summary.addItemSummary(RefreshableItem.CREDITCARD_ACCOUNTS);
        List<Account> creditCardAccounts = agent.fetchCreditCardAccounts().getAccounts();

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching credit card accounts, size: {}",
                creditCardAccounts.size());
        summary.updateItemSummary(
                RefreshableItem.CREDITCARD_ACCOUNTS,
                RefreshableItemFetchingStatus.COMPLETED,
                creditCardAccounts.size());

        context.cacheAccounts(creditCardAccounts);
    }

    private static boolean fetchCreditCardTransactions(
            RefreshCreditCardAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        try {
            log.info("[Refresh Executor Utils] Start fetching credit card transactions.");
            summary.addItemSummary(RefreshableItem.CREDITCARD_TRANSACTIONS);
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchCreditCardTransactions().getTransactions().entrySet();
            List<Integer> fetchedTransactionsCounters =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching credit card transactions, size: {}",
                    fetchedTransactionsCounters);
            summary.updateItemSummary(
                    RefreshableItem.CREDITCARD_TRANSACTIONS,
                    RefreshableItemFetchingStatus.COMPLETED,
                    fetchedTransactionsCounters);
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some credit card account transactions.",
                    e);
            summary.updateItemSummary(
                    RefreshableItem.CREDITCARD_TRANSACTIONS,
                    RefreshableItemFetchingStatus.INTERRUPTED);
            return false;
        }
    }

    private static void refreshAndCacheLoanAccounts(
            RefreshLoanAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching loan accounts.");
        summary.addItemSummary(RefreshableItem.LOAN_ACCOUNTS);
        Map<Account, AccountFeatures> loanAccounts = agent.fetchLoanAccounts().getAccounts();
        for (Map.Entry<Account, AccountFeatures> loanAccount : loanAccounts.entrySet()) {
            context.cacheAccount(loanAccount.getKey(), loanAccount.getValue());
        }

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching loan accounts, size: {}",
                loanAccounts.size());
        summary.updateItemSummary(
                RefreshableItem.LOAN_ACCOUNTS,
                RefreshableItemFetchingStatus.COMPLETED,
                loanAccounts.size());
    }

    private static boolean fetchLoansTransactions(
            RefreshLoanAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        try {
            log.info("[Refresh Executor Utils] Start fetching loans transactions.");
            summary.addItemSummary(RefreshableItem.LOAN_TRANSACTIONS);
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchLoanTransactions().getTransactions().entrySet();
            List<Integer> fetchedTransactionsCounters =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching loans transactions, size: {}",
                    fetchedTransactionsCounters);
            summary.updateItemSummary(
                    RefreshableItem.LOAN_TRANSACTIONS,
                    RefreshableItemFetchingStatus.COMPLETED,
                    fetchedTransactionsCounters);
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some checking loan transactions.", e);
            summary.updateItemSummary(
                    RefreshableItem.LOAN_TRANSACTIONS, RefreshableItemFetchingStatus.INTERRUPTED);
            return false;
        }
    }

    private static void refreshAndCacheInvestmentAccounts(
            RefreshInvestmentAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching investment accounts.");
        summary.addItemSummary(RefreshableItem.INVESTMENT_ACCOUNTS);

        Map<Account, AccountFeatures> investmentAccounts =
                agent.fetchInvestmentAccounts().getAccounts();
        for (Map.Entry<Account, AccountFeatures> investAccount : investmentAccounts.entrySet()) {
            context.cacheAccount(investAccount.getKey(), investAccount.getValue());
        }

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching investment accounts, size: {}",
                investmentAccounts.size());
        summary.updateItemSummary(
                RefreshableItem.INVESTMENT_ACCOUNTS,
                RefreshableItemFetchingStatus.COMPLETED,
                investmentAccounts.size());
    }

    private static boolean fetchInvestmentTransactions(
            RefreshInvestmentAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        try {
            summary.addItemSummary(RefreshableItem.INVESTMENT_TRANSACTIONS);

            log.info("[Refresh Executor Utils] Start fetching investment transactions.");
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchInvestmentTransactions().getTransactions().entrySet();
            List<Integer> fetchedTransactionsCounters =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching investment transactions, size: {}",
                    fetchedTransactionsCounters);
            summary.updateItemSummary(
                    RefreshableItem.INVESTMENT_TRANSACTIONS,
                    RefreshableItemFetchingStatus.COMPLETED,
                    fetchedTransactionsCounters);
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some investment account transactions.",
                    e);
            summary.updateItemSummary(
                    RefreshableItem.INVESTMENT_TRANSACTIONS,
                    RefreshableItemFetchingStatus.INTERRUPTED);
            return false;
        }
    }

    private static void refreshIdentityData(
            RefreshIdentityDataExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Trying to fetch and cache identity data");
        summary.addItemSummary(RefreshableItem.IDENTITY_DATA);
        FetchIdentityDataResponse fetchIdentityDataResponse = agent.fetchIdentityData();
        context.cacheIdentityData(fetchIdentityDataResponse.getIdentityData());

        log.info("[Refresh Executor Utils] Successfully fetched identity data");
        summary.updateItemSummary(
                RefreshableItem.IDENTITY_DATA, RefreshableItemFetchingStatus.COMPLETED);
    }

    private static void refreshEInvoices(RefreshEInvoiceExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching transfer destinations");
        summary.addItemSummary(RefreshableItem.EINVOICES);

        List<Transfer> eInvoices = agent.fetchEInvoices().getEInvoices();
        context.updateEinvoices(eInvoices);

        log.info("[Refresh Executor Utils] Stop fetching einvoices, size: {}", eInvoices.size());
        summary.updateItemSummary(
                RefreshableItem.EINVOICES,
                RefreshableItemFetchingStatus.COMPLETED,
                eInvoices.size());
    }

    private static void refreshTransferDestinations(
            RefreshTransferDestinationExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching transfer destinations");
        summary.addItemSummary(RefreshableItem.TRANSFER_DESTINATIONS);

        Map<Account, List<TransferDestinationPattern>> transferDestinations =
                agent.fetchTransferDestinations(context.getUpdatedAccounts())
                        .getTransferDestinations();
        context.updateTransferDestinationPatterns(transferDestinations);

        List<Integer> fetchingCounters = countFetched(transferDestinations);

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching transfer destinations, size: {}",
                fetchingCounters);
        summary.updateItemSummary(
                RefreshableItem.TRANSFER_DESTINATIONS,
                RefreshableItemFetchingStatus.COMPLETED,
                fetchingCounters);
    }

    private static void refreshListBeneficiaries(
            RefreshBeneficiariesExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();

        log.info("[Refresh Executor Utils] Start fetching beneficiaries");
        summary.addItemSummary(RefreshableItem.LIST_BENEFICIARIES);
        Map<Account, List<TransferDestinationPattern>> beneficiaries =
                agent.fetchBeneficiaries(context.getUpdatedAccounts()).getTransferDestinations();
        context.updateTransferDestinationPatterns(beneficiaries);

        List<Integer> fetchingCounters = countFetched(beneficiaries);

        log.info(
                "[Refresh Executor Utils] Successfully finished fetching beneficiaries, size: {}",
                fetchingCounters);
        summary.updateItemSummary(
                RefreshableItem.LIST_BENEFICIARIES,
                RefreshableItemFetchingStatus.COMPLETED,
                fetchingCounters);
    }

    private static List<Integer> countFetched(
            Map<Account, List<TransferDestinationPattern>> beneficiaries) {
        return beneficiaries.values().stream().map(List::size).collect(Collectors.toList());
    }

    private static List<Integer> updateTransactionsForAccounts(
            AgentContext context,
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet) {
        List<Integer> fetchedTransactionsCounters = new ArrayList<>();

        for (Map.Entry<Account, List<Transaction>> accountTransactionsMap :
                accountTransactionsEntrySet) {
            Account account = accountTransactionsMap.getKey();
            List<Transaction> transactions = accountTransactionsMap.getValue();
            context.updateTransactions(account, transactions);
            fetchedTransactionsCounters.add(transactions.size());
        }

        if (fetchedTransactionsCounters.isEmpty()) {
            fetchedTransactionsCounters.add(0);
        }

        return fetchedTransactionsCounters;
    }

    private static void logIfExtraAccounts(Agent agent, List<Account> accounts) {
        List<AccountTypes> accountTypesExceptCheckingAccounts =
                accounts.stream()
                        .filter(
                                account ->
                                        !ImmutableList.of(AccountTypes.CHECKING, AccountTypes.OTHER)
                                                .contains(account.getType()))
                        .map(Account::getType)
                        .distinct()
                        .collect(Collectors.toList());

        if (!accountTypesExceptCheckingAccounts.isEmpty()) {
            log.error(
                    "[Refresh Executor Utils] Agent {} is asked to fetch checking accounts,"
                            + " But the agent also fetched {} types of accounts",
                    agent.getAgentClass().getName(),
                    accountTypesExceptCheckingAccounts);
            throw new IllegalStateException("Agent fetched other account types as checking");
        }
    }
}

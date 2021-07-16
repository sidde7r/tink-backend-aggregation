package se.tink.backend.aggregation.agents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import se.tink.backend.aggregation.agents.summary.refresh.transactions.OldestTrxDateProvider;
import se.tink.backend.aggregation.agents.summary.refresh.transactions.TransactionsSummary;
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
                    log.warn(
                            "Attempting to refresh EINVOICES. The use of EINVOICES should be removed.");
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
        logStart(RefreshableItem.CHECKING_ACCOUNTS, summary);

        List<Account> checkingAccounts =
                ((RefreshCheckingAccountsExecutor) agent).fetchCheckingAccounts().getAccounts();

        logSuccess(RefreshableItem.CHECKING_ACCOUNTS, checkingAccounts.size(), summary);
        logIfExtraAccounts(agent, checkingAccounts);
        context.cacheAccounts(checkingAccounts);
    }

    private static boolean fetchCheckingTransactions(
            RefreshCheckingAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.CHECKING_TRANSACTIONS, summary);
        try {
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchCheckingTransactions().getTransactions().entrySet();

            TransactionsSummary transactionsSummary =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            logSuccess(RefreshableItem.CHECKING_TRANSACTIONS, transactionsSummary, summary);
            return true;
        } catch (RuntimeException e) {
            logError(RefreshableItem.CHECKING_TRANSACTIONS, e, summary);
            return false;
        }
    }

    private static void refreshAndCacheSavingAccounts(
            RefreshSavingsAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.SAVING_ACCOUNTS, summary);

        List<Account> savingAccounts = agent.fetchSavingsAccounts().getAccounts();

        logSuccess(RefreshableItem.SAVING_ACCOUNTS, savingAccounts.size(), summary);
        context.cacheAccounts(savingAccounts);
    }

    private static boolean fetchSavingTransactions(
            RefreshSavingsAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.SAVING_TRANSACTIONS, summary);

        try {
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchSavingsTransactions().getTransactions().entrySet();

            TransactionsSummary transactionsSummary =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            logSuccess(RefreshableItem.SAVING_TRANSACTIONS, transactionsSummary, summary);
            return true;
        } catch (RuntimeException e) {
            logError(RefreshableItem.SAVING_TRANSACTIONS, e, summary);
            return false;
        }
    }

    private static void refreshAndCacheCreditCardAccounts(
            RefreshCreditCardAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.CREDITCARD_ACCOUNTS, summary);

        List<Account> creditCardAccounts = agent.fetchCreditCardAccounts().getAccounts();

        logSuccess(RefreshableItem.CREDITCARD_ACCOUNTS, creditCardAccounts.size(), summary);
        context.cacheAccounts(creditCardAccounts);
    }

    private static boolean fetchCreditCardTransactions(
            RefreshCreditCardAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.CREDITCARD_TRANSACTIONS, summary);

        try {
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchCreditCardTransactions().getTransactions().entrySet();
            TransactionsSummary transactionsSummary =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            logSuccess(RefreshableItem.CREDITCARD_TRANSACTIONS, transactionsSummary, summary);
            return true;
        } catch (RuntimeException e) {
            logError(RefreshableItem.CREDITCARD_TRANSACTIONS, e, summary);
            return false;
        }
    }

    private static void refreshAndCacheLoanAccounts(
            RefreshLoanAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.LOAN_ACCOUNTS, summary);

        Map<Account, AccountFeatures> loanAccounts = agent.fetchLoanAccounts().getAccounts();
        for (Map.Entry<Account, AccountFeatures> loanAccount : loanAccounts.entrySet()) {
            context.cacheAccount(loanAccount.getKey(), loanAccount.getValue());
        }

        logSuccess(RefreshableItem.LOAN_ACCOUNTS, loanAccounts.size(), summary);
    }

    private static boolean fetchLoansTransactions(
            RefreshLoanAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.LOAN_TRANSACTIONS, summary);

        try {
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchLoanTransactions().getTransactions().entrySet();
            TransactionsSummary transactionsSummary =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            logSuccess(RefreshableItem.LOAN_TRANSACTIONS, transactionsSummary, summary);
            return true;
        } catch (RuntimeException e) {
            logError(RefreshableItem.LOAN_TRANSACTIONS, e, summary);
            return false;
        }
    }

    private static void refreshAndCacheInvestmentAccounts(
            RefreshInvestmentAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.INVESTMENT_ACCOUNTS, summary);

        Map<Account, AccountFeatures> investmentAccounts =
                agent.fetchInvestmentAccounts().getAccounts();
        for (Map.Entry<Account, AccountFeatures> investAccount : investmentAccounts.entrySet()) {
            context.cacheAccount(investAccount.getKey(), investAccount.getValue());
        }

        logSuccess(RefreshableItem.INVESTMENT_ACCOUNTS, investmentAccounts.size(), summary);
    }

    private static boolean fetchInvestmentTransactions(
            RefreshInvestmentAccountsExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.INVESTMENT_TRANSACTIONS, summary);

        try {
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet =
                    agent.fetchInvestmentTransactions().getTransactions().entrySet();
            TransactionsSummary transactionsSummary =
                    updateTransactionsForAccounts(context, accountTransactionsEntrySet);

            logSuccess(RefreshableItem.INVESTMENT_TRANSACTIONS, transactionsSummary, summary);
            return true;
        } catch (RuntimeException e) {
            logError(RefreshableItem.INVESTMENT_TRANSACTIONS, e, summary);
            return false;
        }
    }

    private static void refreshIdentityData(
            RefreshIdentityDataExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.IDENTITY_DATA, summary);

        FetchIdentityDataResponse fetchIdentityDataResponse = agent.fetchIdentityData();
        context.cacheIdentityData(fetchIdentityDataResponse.getIdentityData());

        logSuccess(RefreshableItem.IDENTITY_DATA, summary);
    }

    private static void refreshEInvoices(RefreshEInvoiceExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.EINVOICES, summary);

        List<Transfer> eInvoices = agent.fetchEInvoices().getEInvoices();
        context.updateEinvoices(eInvoices);

        logSuccess(RefreshableItem.EINVOICES, eInvoices.size(), summary);
    }

    private static void refreshTransferDestinations(
            RefreshTransferDestinationExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.TRANSFER_DESTINATIONS, summary);

        Map<Account, List<TransferDestinationPattern>> transferDestinations =
                agent.fetchTransferDestinations(context.getUpdatedAccounts())
                        .getTransferDestinations();
        context.updateTransferDestinationPatterns(transferDestinations);

        logSuccess(
                RefreshableItem.TRANSFER_DESTINATIONS, countFetched(transferDestinations), summary);
    }

    private static void refreshListBeneficiaries(
            RefreshBeneficiariesExecutor agent, AgentContext context) {
        RefreshSummary summary = context.getRefreshSummary();
        logStart(RefreshableItem.LIST_BENEFICIARIES, summary);

        Map<Account, List<TransferDestinationPattern>> beneficiaries =
                agent.fetchBeneficiaries(context.getUpdatedAccounts()).getTransferDestinations();
        context.updateTransferDestinationPatterns(beneficiaries);

        logSuccess(RefreshableItem.LIST_BENEFICIARIES, countFetched(beneficiaries), summary);
    }

    private static List<Integer> countFetched(
            Map<Account, List<TransferDestinationPattern>> beneficiaries) {
        return beneficiaries.values().stream().map(List::size).collect(Collectors.toList());
    }

    private static TransactionsSummary updateTransactionsForAccounts(
            AgentContext context,
            Set<Map.Entry<Account, List<Transaction>>> accountTransactionsEntrySet) {
        List<Integer> fetchedTransactionsCounters = new ArrayList<>();
        Set<LocalDate> dates = new HashSet<>();

        for (Map.Entry<Account, List<Transaction>> accountTransactionsMap :
                accountTransactionsEntrySet) {
            Account account = accountTransactionsMap.getKey();
            List<Transaction> transactions = accountTransactionsMap.getValue();
            context.updateTransactions(account, transactions);
            fetchedTransactionsCounters.add(transactions.size());

            OldestTrxDateProvider.getDate(transactions).ifPresent(dates::add);
        }

        if (fetchedTransactionsCounters.isEmpty()) {
            fetchedTransactionsCounters.add(0);
        }

        LocalDate oldestTransactionDate = dates.stream().min(LocalDate::compareTo).orElse(null);
        return new TransactionsSummary(fetchedTransactionsCounters, oldestTransactionDate);
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

    private static void logStart(RefreshableItem item, RefreshSummary summary) {
        log.info("[Refresh Executor Utils] Start fetching {}", item);
        summary.addItemSummary(item);
    }

    private static void logSuccess(RefreshableItem item, RefreshSummary summary) {
        logSuccess(item, Collections.emptyList(), summary);
    }

    private static void logSuccess(RefreshableItem item, int fetched, RefreshSummary summary) {
        logSuccess(item, Collections.singletonList(fetched), summary);
    }

    private static void logSuccess(
            RefreshableItem item, TransactionsSummary transactionsSummary, RefreshSummary summary) {
        logSuccess(
                item,
                transactionsSummary.getFetched(),
                transactionsSummary.getOldestTransactionDate(),
                summary);
    }

    private static void logSuccess(
            RefreshableItem item, List<Integer> fetched, RefreshSummary summary) {
        logSuccess(item, fetched, null, summary);
    }

    private static void logSuccess(
            RefreshableItem item,
            List<Integer> fetched,
            LocalDate oldestTransactionDate,
            RefreshSummary summary) {
        StringBuilder msg =
                new StringBuilder()
                        .append("[Refresh Executor Utils] Successfully finished fetching ")
                        .append(item);
        if (!fetched.isEmpty()) {
            msg.append(", size: ").append(fetched);
        }

        log.info(msg.toString());
        summary.updateItemSummary(
                item, RefreshableItemFetchingStatus.COMPLETED, fetched, oldestTransactionDate);
    }

    private static void logError(RefreshableItem item, Exception e, RefreshSummary summary) {
        log.error("[Refresh Executor Utils] Failed during fetching {}", item, e);
        summary.updateItemSummary(item, RefreshableItemFetchingStatus.INTERRUPTED);
    }
}

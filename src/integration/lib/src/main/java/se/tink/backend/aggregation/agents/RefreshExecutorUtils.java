package se.tink.backend.aggregation.agents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
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
        log.info("[Refresh Executor Utils] Start fetching checking accounts.");
        List<Account> checkingAccounts =
                ((RefreshCheckingAccountsExecutor) agent).fetchCheckingAccounts().getAccounts();
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching checking accounts, size: {}",
                checkingAccounts.size());
        logIfExtraAccounts(agent, checkingAccounts);
        context.cacheAccounts(checkingAccounts);
    }

    private static boolean fetchCheckingTransactions(
            RefreshCheckingAccountsExecutor agent, AgentContext context) {
        try {
            log.info("[Refresh Executor Utils] Start fetching checking transactions.");
            for (Map.Entry<Account, List<Transaction>> accountTransactions :
                    agent.fetchCheckingTransactions().getTransactions().entrySet()) {
                context.updateTransactions(
                        accountTransactions.getKey(), accountTransactions.getValue());
            }
            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching checking transactions.");
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some checking account transactions.",
                    e);
            return false;
        }
    }

    private static void refreshAndCacheSavingAccounts(
            RefreshSavingsAccountsExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching saving accounts.");
        List<Account> savingAccounts = agent.fetchSavingsAccounts().getAccounts();
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching saving accounts, size: {}",
                savingAccounts.size());
        context.cacheAccounts(savingAccounts);
    }

    private static boolean fetchSavingTransactions(
            RefreshSavingsAccountsExecutor agent, AgentContext context) {
        try {
            log.info("[Refresh Executor Utils] Start fetching saving transactions.");
            for (Map.Entry<Account, List<Transaction>> accountTransactions :
                    agent.fetchSavingsTransactions().getTransactions().entrySet()) {
                context.updateTransactions(
                        accountTransactions.getKey(), accountTransactions.getValue());
            }
            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching saving transactions.");
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some saving account transactions.",
                    e);
            return false;
        }
    }

    private static void refreshAndCacheCreditCardAccounts(
            RefreshCreditCardAccountsExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching credit card accounts.");
        List<Account> creditCardAccounts = agent.fetchCreditCardAccounts().getAccounts();
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching credit card accounts, size: {}",
                creditCardAccounts.size());
        context.cacheAccounts(creditCardAccounts);
    }

    private static boolean fetchCreditCardTransactions(
            RefreshCreditCardAccountsExecutor agent, AgentContext context) {
        try {
            log.info("[Refresh Executor Utils] Start fetching credit card transactions.");
            for (Map.Entry<Account, List<Transaction>> accountTransactions :
                    agent.fetchCreditCardTransactions().getTransactions().entrySet()) {
                context.updateTransactions(
                        accountTransactions.getKey(), accountTransactions.getValue());
            }
            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching credit card transactions.");
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some credit card account transactions.",
                    e);
            return false;
        }
    }

    private static void refreshAndCacheLoanAccounts(
            RefreshLoanAccountsExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching loan accounts.");
        Map<Account, AccountFeatures> loanAccounts = agent.fetchLoanAccounts().getAccounts();
        for (Map.Entry<Account, AccountFeatures> loanAccount : loanAccounts.entrySet()) {
            context.cacheAccount(loanAccount.getKey(), loanAccount.getValue());
        }
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching loan accounts, size: {}",
                loanAccounts.size());
    }

    private static boolean fetchLoansTransactions(
            RefreshLoanAccountsExecutor agent, AgentContext context) {
        try {
            log.info("[Refresh Executor Utils] Start fetching loans transactions.");
            for (Map.Entry<Account, List<Transaction>> accountTransactions :
                    agent.fetchLoanTransactions().getTransactions().entrySet()) {
                context.updateTransactions(
                        accountTransactions.getKey(), accountTransactions.getValue());
            }
            log.info("[Refresh Executor Utils] Successfully finished fetching loans transactions.");
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some checking loan transactions.", e);
            return false;
        }
    }

    private static void refreshAndCacheInvestmentAccounts(
            RefreshInvestmentAccountsExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching investment accounts.");
        Map<Account, AccountFeatures> investmentAccounts =
                agent.fetchInvestmentAccounts().getAccounts();
        for (Map.Entry<Account, AccountFeatures> investAccount : investmentAccounts.entrySet()) {
            context.cacheAccount(investAccount.getKey(), investAccount.getValue());
        }
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching investment accounts, size: {}",
                investmentAccounts.size());
    }

    private static boolean fetchInvestmentTransactions(
            RefreshInvestmentAccountsExecutor agent, AgentContext context) {
        try {
            log.info("[Refresh Executor Utils] Start fetching investment transactions.");
            for (Map.Entry<Account, List<Transaction>> accountTransactions :
                    agent.fetchInvestmentTransactions().getTransactions().entrySet()) {
                context.updateTransactions(
                        accountTransactions.getKey(), accountTransactions.getValue());
            }
            log.info(
                    "[Refresh Executor Utils] Successfully finished fetching investment transactions.");
            return true;
        } catch (RuntimeException e) {
            log.error(
                    "[Refresh Executor Utils] Failed to fetch some investment account transactions.",
                    e);
            return false;
        }
    }

    private static void refreshIdentityData(
            RefreshIdentityDataExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Trying to fetch and cache identity data");
        FetchIdentityDataResponse fetchIdentityDataResponse = agent.fetchIdentityData();
        context.cacheIdentityData(fetchIdentityDataResponse.getIdentityData());
        log.info("[Refresh Executor Utils] Successfully fetched identity data");
    }

    private static void refreshEInvoices(RefreshEInvoiceExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching transfer destinations");
        List<Transfer> eInvoices = agent.fetchEInvoices().getEInvoices();
        context.updateEinvoices(eInvoices);
        log.info("[Refresh Executor Utils] Stop fetching einvoices, size: {}", eInvoices.size());
    }

    private static void refreshTransferDestinations(
            RefreshTransferDestinationExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching transfer destinations");
        Map<Account, List<TransferDestinationPattern>> transferDestinations =
                agent.fetchTransferDestinations(context.getUpdatedAccounts())
                        .getTransferDestinations();
        context.updateTransferDestinationPatterns(transferDestinations);
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching transfer destinations, size: {}",
                transferDestinations.values().size());
    }

    private static void refreshListBeneficiaries(
            RefreshBeneficiariesExecutor agent, AgentContext context) {
        log.info("[Refresh Executor Utils] Start fetching beneficiaries");
        Map<Account, List<TransferDestinationPattern>> beneficiaries =
                agent.fetchBeneficiaries(context.getUpdatedAccounts()).getTransferDestinations();
        context.updateTransferDestinationPatterns(beneficiaries);
        log.info(
                "[Refresh Executor Utils] Successfully finished fetching beneficiaries, size: {}",
                beneficiaries.values().size());
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

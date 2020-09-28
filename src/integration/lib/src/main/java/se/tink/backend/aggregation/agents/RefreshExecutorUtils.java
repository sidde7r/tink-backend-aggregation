package se.tink.backend.aggregation.agents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.credentials.service.RefreshableItem;

public final class RefreshExecutorUtils {
    private static Logger log = LoggerFactory.getLogger(RefreshExecutorUtils.class);

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

    public static void executeSegregatedRefresher(
            Agent agent, RefreshableItem item, AgentContext context) {
        Class executorKlass = RefreshExecutorUtils.getRefreshExecutor(item);
        if (executorKlass == null) {
            throw new NotImplementedException(
                    String.format("No implementation for %s", item.name()));
        }
        // Segregated refresh executor
        if (executorKlass.isAssignableFrom(agent.getAgentClass())) {
            switch (item) {
                case EINVOICES:
                    context.updateEinvoices(
                            ((RefreshEInvoiceExecutor) agent).fetchEInvoices().getEInvoices());
                    break;
                case TRANSFER_DESTINATIONS:
                    context.updateTransferDestinationPatterns(
                            ((RefreshTransferDestinationExecutor) agent)
                                    .fetchTransferDestinations(context.getUpdatedAccounts())
                                    .getTransferDestinations());
                    break;
                case LIST_BENEFICIARIES:
                    context.updateTransferDestinationPatterns(
                            ((RefreshBeneficiariesExecutor) agent)
                                    .fetchBeneficiaries(context.getUpdatedAccounts())
                                    .getTransferDestinations());
                    break;
                case CHECKING_ACCOUNTS:
                    cacheCheckingAccounts(agent, context);
                    break;
                case CHECKING_TRANSACTIONS:
                    for (Map.Entry<Account, List<Transaction>> accountTransactions :
                            ((RefreshCheckingAccountsExecutor) agent)
                                    .fetchCheckingTransactions()
                                    .getTransactions()
                                    .entrySet()) {
                        context.updateTransactions(
                                accountTransactions.getKey(), accountTransactions.getValue());
                    }
                    break;
                case SAVING_ACCOUNTS:
                    List<Account> savingAccounts =
                            ((RefreshSavingsAccountsExecutor) agent)
                                    .fetchSavingsAccounts()
                                    .getAccounts();
                    log.info("size of savingAccounts: {}", savingAccounts.size());
                    context.cacheAccounts(savingAccounts);
                    break;
                case SAVING_TRANSACTIONS:
                    for (Map.Entry<Account, List<Transaction>> accountTransactions :
                            ((RefreshSavingsAccountsExecutor) agent)
                                    .fetchSavingsTransactions()
                                    .getTransactions()
                                    .entrySet()) {
                        context.updateTransactions(
                                accountTransactions.getKey(), accountTransactions.getValue());
                    }
                    break;
                case CREDITCARD_ACCOUNTS:
                    List<Account> creditCardAccounts =
                            ((RefreshCreditCardAccountsExecutor) agent)
                                    .fetchCreditCardAccounts()
                                    .getAccounts();
                    log.info("size of creditCardAccounts: {}", creditCardAccounts.size());
                    context.cacheAccounts(creditCardAccounts);
                    break;
                case CREDITCARD_TRANSACTIONS:
                    for (Map.Entry<Account, List<Transaction>> accountTransactions :
                            ((RefreshCreditCardAccountsExecutor) agent)
                                    .fetchCreditCardTransactions()
                                    .getTransactions()
                                    .entrySet()) {
                        context.updateTransactions(
                                accountTransactions.getKey(), accountTransactions.getValue());
                    }
                    break;
                case LOAN_ACCOUNTS:
                    Map<Account, AccountFeatures> loanAccounts =
                            ((RefreshLoanAccountsExecutor) agent).fetchLoanAccounts().getAccounts();
                    log.info("size of loanAccounts: {}", loanAccounts.size());
                    for (Map.Entry<Account, AccountFeatures> loanAccount :
                            loanAccounts.entrySet()) {
                        context.cacheAccount(loanAccount.getKey(), loanAccount.getValue());
                    }
                    break;
                case LOAN_TRANSACTIONS:
                    for (Map.Entry<Account, List<Transaction>> accountTransactions :
                            ((RefreshLoanAccountsExecutor) agent)
                                    .fetchLoanTransactions()
                                    .getTransactions()
                                    .entrySet()) {
                        context.updateTransactions(
                                accountTransactions.getKey(), accountTransactions.getValue());
                    }
                    break;
                case INVESTMENT_ACCOUNTS:
                    Map<Account, AccountFeatures> investmentAccounts =
                            ((RefreshInvestmentAccountsExecutor) agent)
                                    .fetchInvestmentAccounts()
                                    .getAccounts();
                    log.info("size of investmentAccounts: {}", investmentAccounts.size());
                    for (Map.Entry<Account, AccountFeatures> investAccount :
                            investmentAccounts.entrySet()) {
                        context.cacheAccount(investAccount.getKey(), investAccount.getValue());
                    }
                    break;
                case INVESTMENT_TRANSACTIONS:
                    for (Map.Entry<Account, List<Transaction>> accountTransactions :
                            ((RefreshInvestmentAccountsExecutor) agent)
                                    .fetchInvestmentTransactions()
                                    .getTransactions()
                                    .entrySet()) {
                        context.updateTransactions(
                                accountTransactions.getKey(), accountTransactions.getValue());
                    }
                    break;
                case IDENTITY_DATA:
                    log.info("Trying to fetch and cache identity data");
                    context.cacheIdentityData(
                            ((RefreshIdentityDataExecutor) agent)
                                    .fetchIdentityData()
                                    .getIdentityData());
                    break;
                default:
                    throw new IllegalStateException(
                            String.format("Invalid refreshable item detected %s", item.name()));
            }
        } else {
            log.warn(
                    "A request for {} is received, agent is not capable of doing this",
                    item.name());
        }
    }

    private static void cacheCheckingAccounts(Agent agent, AgentContext context) {
        List<Account> checkingAccounts =
                ((RefreshCheckingAccountsExecutor) agent).fetchCheckingAccounts().getAccounts();

        logIfFetchedExtraAccounts(agent, checkingAccounts);
        log.info("size of checkingAccounts: {}", checkingAccounts.size());

        context.cacheAccounts(checkingAccounts);
    }

    private static void logIfFetchedExtraAccounts(Agent agent, List<Account> accounts) {

        List<AccountTypes> accountTypesExceptCheckingAccounts =
                accounts.stream()
                        .filter(
                                account ->
                                        !ImmutableList.of(AccountTypes.CHECKING, AccountTypes.OTHER)
                                                .contains(account.getType()))
                        .map(account -> account.getType())
                        .distinct()
                        .collect(Collectors.toList());

        if (!accountTypesExceptCheckingAccounts.isEmpty()) {
            log.warn(
                    "Agent {} is asked to fetch checking accounts,"
                            + " But the agent also fetched {} types of accounts",
                    agent.getAgentClass().getName(),
                    accountTypesExceptCheckingAccounts);
        }
    }
}

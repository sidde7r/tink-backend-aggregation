package se.tink.backend.aggregation.agents.framework;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.utils.CliPrintUtils;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DualAgentIntegrationTest {

    private final AgentIntegrationTest first;
    private final AgentIntegrationTest second;
    private static final Logger log = LoggerFactory.getLogger(DualAgentIntegrationTest.class);

    private DualAgentIntegrationTest(AgentIntegrationTest first, AgentIntegrationTest second) {
        this.first = first;
        this.second = second;
    }

    public static DualAgentIntegrationTest of(
            AgentIntegrationTest first, AgentIntegrationTest second) {
        return new DualAgentIntegrationTest(first, second);
    }

    public void testAndCompare() throws Exception {
        NewAgentTestContext firstContext = first.testRefresh();
        NewAgentTestContext secondContext = second.testRefresh();
        String firstName = first.getProvider().getName();
        String secondName = second.getProvider().getName();

        // Build data
        Map<String, Pair<Account, Account>> accounts = mergeAccounts(firstContext, secondContext);
        Map<String, Map<String, Pair<Transaction, Transaction>>> transactions =
                Stream.concat(
                                firstContext.getUpdatedAccounts().stream(),
                                secondContext.getUpdatedAccounts().stream())
                        .map(Account::getBankId)
                        .distinct()
                        .map(
                                accountId ->
                                        new Pair<>(
                                                accountId,
                                                mergeTransactions(
                                                        firstContext, secondContext, accountId)))
                        .collect(Collectors.toMap(p -> p.first, p -> p.second));

        log.info("[REFRESH SUMMARY]\n" + firstContext.getRefreshSummary().toJson());
        // Log data obtained from both agents
        log.info("[[[ {} CONTEXT DATA ]]]\n\n", firstName);
        firstContext.printCollectedData();

        log.info("[REFRESH SUMMARY]\n" + secondContext.getRefreshSummary().toJson());
        log.info("[[[ {} CONTEXT DATA ]]]\n\n", secondName);
        secondContext.printCollectedData();

        log.info("[[[ DUAL TEST ]]]\n\n");

        if (accounts.size() > 0) {
            logAccounts(firstName, secondName, accounts, transactions);
        } else {
            log.info("[SUCCESS] All accounts in both agents are identical!");
        }

        transactions.forEach(
                (accountId, transactionMap) -> {
                    log.info("[[[ Transactions for account ID {} ]]]", accountId);
                    if (transactionMap.size() > 0) {
                        logTransactions(firstName, secondName, accountId, transactionMap);
                    } else {
                        log.info("[SUCCESS] All transactions in both agents are identical!");
                    }
                    log.info("");
                });

        assertEquals(0, accounts.keySet().size());
        assertEquals(0, transactions.values().stream().mapToInt(Map::size).sum());
    }

    private void logAccounts(
            String firstName,
            String secondName,
            Map<String, Pair<Account, Account>> accounts,
            Map<String, Map<String, Pair<Transaction, Transaction>>> transactions) {
        log.warn("[FAIL] There are accounts not present in both agents, or with differences.");

        log.warn("[FAIL] Accounts present in {} but not in {}:", firstName, secondName);
        List<Map<String, String>> firstAccounts =
                accounts.values().stream()
                        .filter(pair -> pair.second == null)
                        .map(pair -> pair.first)
                        .map(getAccountTableMapper())
                        .collect(Collectors.toList());

        CliPrintUtils.printTable(0, "Accounts " + firstName, firstAccounts);

        log.warn("[FAIL] Accounts present in {} but not in {}:", secondName, firstName);
        List<Map<String, String>> secondAccounts =
                accounts.values().stream()
                        .filter(pair -> pair.first == null)
                        .map(pair -> pair.second)
                        .map(getAccountTableMapper())
                        .collect(Collectors.toList());

        CliPrintUtils.printTable(0, "Accounts " + secondName, secondAccounts);

        log.info("Accounts where data *differed* between {} and {}", firstName, secondName);
        accounts.entrySet().stream()
                .filter(
                        entry ->
                                !Account.deepEquals(
                                        entry.getValue().first, entry.getValue().second))
                .filter(entry -> entry.getValue().first != null && entry.getValue().second != null)
                .forEach(
                        pair -> {
                            log.warn(
                                    "[FAIL] Account data *differed* for account with ID {}",
                                    pair.getKey());

                            Pair<Account, Account> value = pair.getValue();
                            log.warn(
                                    "{}\n{}\n\n",
                                    SerializationUtils.serializeToString(value.first),
                                    SerializationUtils.serializeToString(value.second));
                        });
    }

    private void logTransactions(
            String firstName,
            String secondName,
            String accountId,
            Map<String, Pair<Transaction, Transaction>> transactionMap) {
        log.warn(">>> {}", accountId);
        log.warn("[FAIL] Found suspicious transactions for account: {}", accountId);
        log.warn("[FAIL] There are transactions not present in both agents, or with differences.");

        List<Map<String, String>> firstTransactions =
                transactionMap.values().stream()
                        .filter(pair -> pair.second == null)
                        .map(pair -> pair.first)
                        .sorted(Comparator.comparing(Transaction::getDate))
                        .map(getTransactionTableMapper())
                        .collect(Collectors.toList());

        if (firstTransactions.size() > 0) {
            log.warn(
                    "[FAIL] Transactions in account {} present in {} but not in {}:",
                    accountId,
                    firstName,
                    secondName);
            CliPrintUtils.printTable(
                    4, "Transactions [" + accountId + "] " + firstName, firstTransactions);
        }

        List<Map<String, String>> secondTransactions =
                transactionMap.values().stream()
                        .filter(pair -> pair.first == null)
                        .map(pair -> pair.second)
                        .sorted(Comparator.comparing(Transaction::getDate))
                        .map(getTransactionTableMapper())
                        .collect(Collectors.toList());

        if (secondTransactions.size() > 0) {
            log.warn(
                    "[FAIL] Transactions in account {} present in {} but not in {}:",
                    accountId,
                    secondName,
                    firstName);
            CliPrintUtils.printTable(
                    4, "Transactions [" + accountId + "] " + secondName, secondTransactions);
        }

        transactionMap.entrySet().stream()
                .filter(
                        entry ->
                                !Transaction.deepEquals(
                                        entry.getValue().first, entry.getValue().second))
                .filter(entry -> entry.getValue().first != null && entry.getValue().second != null)
                .forEach(
                        pair -> {
                            log.warn(
                                    "[FAIL] Transaction data *differed* for transaction with ID {}",
                                    pair.getKey());

                            Pair<Transaction, Transaction> value = pair.getValue();
                            log.warn(
                                    "{}\n{}\n\n",
                                    SerializationUtils.serializeToString(value.first),
                                    SerializationUtils.serializeToString(value.second));
                        });
    }

    private Map<String, Pair<Account, Account>> mergeAccounts(
            NewAgentTestContext firstContext, NewAgentTestContext secondContext) {
        return pairingMerger(
                account ->
                        account.getType() == AccountTypes.CHECKING
                                || account.getType() == AccountTypes.SAVINGS,
                Account::getBankId,
                entry -> !Account.deepEquals(entry.getValue().first, entry.getValue().second),
                firstContext.getUpdatedAccounts(),
                secondContext.getUpdatedAccounts());
    }

    private Map<String, Pair<Transaction, Transaction>> mergeTransactions(
            NewAgentTestContext firstContext, NewAgentTestContext secondContext, String accountId) {
        return pairingMerger(
                t -> true,
                t -> String.format("%s%s", t.getDate().getTime(), t.getAmount()),
                entry -> !Transaction.deepEquals(entry.getValue().first, entry.getValue().second),
                firstContext.getTransactionsToProcessByBankAccountId(accountId),
                secondContext.getTransactionsToProcessByBankAccountId(accountId));
    }

    /**
     * Constructs a map of {@link Pair}s of elements from two lists, where the key is the identity,
     * and the value is a pair of elements.
     *
     * <ul>
     *   <li>If an element with the ID is present in the first list, the first part of the pair
     *       contains said element.
     *   <li>If an element with the ID is present in the second list, the second part of the pair
     *       contains said element.
     *   <li>Hence, if an element with the ID is present in both lists, both parts of the pair
     *       contain data.
     * </ul>
     *
     * Because we usually want to find elements which differ from another, a filter predicate can be
     * supplied to filter out elements which are present in both lists and identical. The ID is
     * obtained using the supplied identity function.
     *
     * <p>(This whole method is essentially a FULL OUTER JOIN, but with streams.)
     *
     * @param preFilter Filter predicate run before processing begins, to filter out unwanted data.
     * @param identityFunction Function to get the ID based on an element, usually something like
     *     <pre>Entity::getId</pre>
     *
     * @param filter Filter predicate run after merging. Will keep only entries for which the filter
     *     returns true. Probably something like
     *     <pre>entry -> !Objects.equals(entry.getValue().first, entry.getValue().second)</pre>
     *
     * @param first First list of elements
     * @param second Second list of elements
     * @param <T> Type of the elements
     * @return Map, with IDs as keys, and {@link Pair}s as values (see structure explanation above)
     */
    private <T> Map<String, Pair<T, T>> pairingMerger(
            Predicate<T> preFilter,
            Function<T, String> identityFunction,
            Predicate<Entry<String, Pair<T, T>>> filter,
            List<T> first,
            List<T> second) {
        return Stream.concat(
                        first.stream()
                                .filter(preFilter)
                                .map(
                                        item ->
                                                new Pair<>(
                                                        identityFunction.apply(item),
                                                        new Pair<T, T>(item, null))),
                        second.stream()
                                .filter(preFilter)
                                .map(
                                        item ->
                                                new Pair<>(
                                                        identityFunction.apply(item),
                                                        new Pair<T, T>(null, item))))
                .collect(
                        Collectors.toMap(
                                p -> p.first,
                                p -> p.second,
                                (a, b) ->
                                        Pair.of(
                                                a.first != null ? a.first : b.first,
                                                a.second != null ? a.second : b.second)))
                .entrySet().stream()
                .filter(filter)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Function<Account, Map<String, String>> getAccountTableMapper() {
        return account -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("accountId", account.getBankId());
            row.put("type", account.getType().name());
            row.put("number", account.getAccountNumber());
            row.put("name", account.getName());
            row.put("balance", String.valueOf(account.getBalance()));
            return row;
        };
    }

    private Function<Transaction, Map<String, String>> getTransactionTableMapper() {
        return transaction -> {
            Map<String, String> row = new LinkedHashMap<>();
            row.put(
                    "date",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(transaction.getDate()));
            row.put("description", transaction.getDescription());
            row.put("amount", String.valueOf(transaction.getAmount()));
            return row;
        };
    }
}

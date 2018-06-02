package se.tink.backend.system.workers.processor.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(GuiceRunner.class)
public class SaveTransactionCommandTest  {
    private List<User> users;
    private Map<String, Credentials> credentials;

    List<Category> categories;
    @Inject
    private TestUtil testUtil;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private TestProcessor transactionProcessor;

    @Inject
    private CredentialsRepository credentialsRepository;

    @Inject
    private LoanDataRepository loanDataRepository;

    @Inject
    private TransactionDao transactionDao;

    @Inject
    private AccountRepository accountRepository;


    private MetricRegistry metricRegistry;

    @Before
    public void setUp() throws Exception {
        users = testUtil.getTestUsers("SaveTransactionCommandTest");
        categories = categoryRepository.findAll();
        credentials = users.stream().map(user ->{
            return testUtil.getCredentials(user, "swedbank-bankid");
        }).collect(Collectors.toMap(c -> c.getUserId(), c -> c));
        metricRegistry = new MetricRegistry();
    }

    @Test
    public void testSaveTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,10, "test");
            TransactionProcessorContext context = saveTransactions(user, transactions);
            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());

            assertTransactions(transactions, savedTransaction);
        });
    }

    @Test
    public void testUpdateAllTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            saveTransactions(user, transactions);
            changeDescription(transactions, "change");
            TransactionProcessorContext context = saveTransactions(user, transactions, transactions, null);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());

            assertTransactions(transactions, savedTransaction);
        });
    }

    @Test
    public void testUpdateOldTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            saveTransactions(user, transactions);
            changeDescription(transactions, "change");
            List<Transaction> newTransactions = getTransaction( user,2, "new");
            newTransactions.addAll(transactions);
            TransactionProcessorContext context = saveTransactions(user, newTransactions, transactions, null);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());

            assertTransactions(newTransactions, savedTransaction);
        });
    }

    @Test
    public void testUpdateOneTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            transactions.get(0).setDescription("changed");
            TransactionProcessorContext context = saveTransactions(user, transactions, Lists.newArrayList(transactions.get(0)), null);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());

            assertTransactions(transactions, savedTransaction);
        });
    }

    @Test
    public void testDeleteAllTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            saveTransactions(user, transactions);
            List<Transaction> newTransactions = getTransaction( user,2, "new");
            transactions.addAll(newTransactions);
            TransactionProcessorContext context = saveTransactions(user, newTransactions, null, transactions);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !transactions.contains(t)).collect(Collectors.toList());
            assertTransactions(Lists.<Transaction>newArrayList(), allTransactions);
        });
    }

    @Test
    public void testDeleteOldTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            saveTransactions(user, transactions);
            List<Transaction> newTransactions = getTransaction( user,2, "new");
            TransactionProcessorContext context = saveTransactions(user, newTransactions, null, transactions);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !transactions.contains(t)).collect(Collectors.toList());

            assertTransactions(newTransactions, allTransactions);
        });
    }

    @Test
    public void testDeleteOneTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            ArrayList<Transaction> deleted = Lists.newArrayList(transactions.get(0));
            TransactionProcessorContext context = saveTransactions(user, transactions, null, deleted);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !deleted.contains(t)).collect(Collectors.toList());

            assertTransactions(transactions.subList(1, 3), allTransactions);
        });
    }

    @Test
    public void testUpdateAllAndDeleteAllTransactions(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,3, "test");
            changeDescription(transactions, "change");
            TransactionProcessorContext context = saveTransactions(user, transactions, transactions, transactions);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !transactions.contains(t)).collect(Collectors.toList());

            assertTransactions(Lists.<Transaction>newArrayList(), allTransactions);
        });
    }

    @Test
    public void testSavePartUpdatePartAndDeletePartTransactions(){
        users.forEach(user -> {
            List<Transaction> deleteTransactions = getTransaction( user,3, "delete");
            saveTransactions(user,deleteTransactions);

            List<Transaction> updateTransaction = getTransaction( user,3, "update");
            changeDescription(updateTransaction, "change");

            List<Transaction> transactions = getTransaction( user,4, "save");
            transactions.addAll(updateTransaction);

            TransactionProcessorContext context = saveTransactions(user, transactions, updateTransaction, deleteTransactions);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !deleteTransactions.contains(t)).collect(Collectors.toList());

            assertTransactions(transactions, allTransactions);
        });
    }

    @Test
    public void testSavePartUpdatePartAndDeletePartTransactions2(){
        users.forEach(user -> {
            List<Transaction> deleteTransactions = getTransaction( user,3, "delete");

            List<Transaction> updateTransaction = getTransaction( user,3, "update");
            changeDescription(updateTransaction, "change");

            List<Transaction> saveTransactions = getTransaction( user,4, "save");

            List<Transaction> transactions = Lists.newArrayList(saveTransactions);
            transactions.addAll(updateTransaction);
            transactions.addAll(deleteTransactions);

            TransactionProcessorContext context = saveTransactions(user, transactions, updateTransaction, deleteTransactions);
            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !deleteTransactions.contains(t)).collect(Collectors.toList());
            List<Transaction> expTransactions = Lists.newArrayList(saveTransactions);
            expTransactions.addAll(updateTransaction);

            assertTransactions(expTransactions, allTransactions);
        });
    }

    @Test
    public void testSavePartUpdatePartAndDeletePartTransactions3(){
        users.forEach(user -> {
            List<Transaction> deleteTransactions = getTransaction( user,3, "delete");
            saveTransactions(user, deleteTransactions);

            List<Transaction> updateTransaction = getTransaction( user,3, "update");
            saveTransactions(user, updateTransaction);
            changeDescription(updateTransaction, "change");

            List<Transaction> transactions = getTransaction( user,4, "save");
            transactions.addAll(updateTransaction);

            TransactionProcessorContext context = saveTransactions(user, transactions, updateTransaction, deleteTransactions);

            List<Transaction> savedTransaction = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            List<Transaction> allTransactions = savedTransaction.stream().filter(t -> !deleteTransactions.contains(t)).collect(Collectors.toList());

            assertTransactions(transactions, allTransactions);
        });
    }

    @Test
    public void failSaveTransactionsNullDate(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,10, "test");
            transactions.get(0).setDate(null);
            saveTransactions(user, transactions);
            Counter transactionsSaved = (Counter)metricRegistry.get(MetricId.newId("transactions_saved"));

            Assert.assertEquals(0, transactionsSaved.getCount());
        });
    }

    @Test
    public void failSaveTransactionsNullDescription(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,10, "test");
            transactions.get(0).setDescription(null);
            saveTransactions(user, transactions);

            Counter transactionsSaved = (Counter)metricRegistry.get(MetricId.newId("transactions_saved"));

            Assert.assertEquals(0, transactionsSaved.getCount());
        });
    }

    @Test
    public void failSaveTransactionsNullType(){
        users.forEach(user -> {
            List<Transaction> transactions = getTransaction( user,10, "test");
            transactions.get(0).setType(null);
            saveTransactions(user, transactions);

            Counter transactionsSaved = (Counter)metricRegistry.get(MetricId.newId("transactions_saved"));

            Assert.assertEquals(0, transactionsSaved.getCount());
        });
    }


    private void assertTransactions(List<Transaction> expTransactions, List<Transaction> actTransactions){
        Assert.assertEquals(expTransactions.size(), actTransactions.size());

        Set<String> descriptions = getDescription(actTransactions);
        for (Transaction transaction : expTransactions) {
            Assert.assertTrue(descriptions.contains(transaction.getDescription()));
        }
    }

    private Set<String> getDescription(List<Transaction> transactions){
        return transactions.stream()
                .map(t -> t.getDescription())
                .collect(Collectors.toSet());
    }

    private List<Transaction> getTransaction(User user, int n, String label){
        List<Transaction> transactions = Lists.newArrayList();

        for (int i = 0; i < n; i++) {
            Transaction transaction = testUtil.getNewTransaction(user.getId(), -i, label + i);
            transaction.setCategory(categories.get(i % categories.size()));
            transactions.add(transaction);
        }

        return transactions;
    }

    private TransactionProcessorContext saveTransactions(User user, List<Transaction> transactions) {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(Lists.newArrayList(credentials.get(user.getId())));
            userData.setTransactions(Lists.<Transaction>newArrayList());

            TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions,
                    userData,
                    credentials.get(user.getId()).getId()
            );

            transactionProcessor.process(
                    context,
                    userData,
                    () -> new SimpleChainFactory(ctx ->
                            ImmutableList.of(
                                    new LoadUserDataCommand(
                                            ctx, credentialsRepository, loanDataRepository, transactionDao,
                                            accountRepository
                                    ),
                                    new PrepareTransactionsToSaveAndDeleteCommand(ctx,
                                            metricRegistry),
                                    new SaveTransactionCommand(ctx, transactionDao, metricRegistry)
                            )
                    ),
                    false);
            return context;
    }

    private TransactionProcessorContext saveTransactions(User user, List<Transaction> transactions, List<Transaction> updated, List<Transaction> deleted) {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(Lists.newArrayList(credentials.get(user.getId())));
            userData.setTransactions(transactions);

            TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions,
                    userData,
                    credentials.get(user.getId()).getId()
            );

            if (updated != null) {
                for (Transaction transaction : updated) {
                    context.addTransactionToUpdateListPresentInDb(transaction.getId());
                }
            }

            if (deleted != null) {
                for (Transaction transaction : deleted) {
                    context.addTransactionToDelete(transaction);
                }
            }

            transactionProcessor.process(
                    context,
                    userData,
                    () -> new SimpleChainFactory(ctx ->
                            ImmutableList.of(
                                    new LoadUserDataCommand(
                                            ctx, credentialsRepository, loanDataRepository, transactionDao,
                                            accountRepository
                                    ),
                                    new SaveTransactionCommand(ctx, transactionDao, metricRegistry)
                            )
                    ),
                    true);
        return context;
    }

    private void changeDescription(List<Transaction> transactions, String description){
        for (int i = 0; i < transactions.size(); i++) {
            transactions.get(i).setDescription(description + i);
        }
    }

}

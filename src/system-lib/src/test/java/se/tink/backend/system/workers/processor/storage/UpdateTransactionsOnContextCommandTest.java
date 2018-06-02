package se.tink.backend.system.workers.processor.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.metrics.MetricRegistry;

@RunWith(GuiceRunner.class)
public class UpdateTransactionsOnContextCommandTest {

    private List<User> users;
    private List<Category> categories;
    private Map<String,TransactionProcessorContext> contexts;
    private Map<String,UserData> userDatas;
    private Map<String, Credentials> credentials;

    @Inject
    TestUtil testUtil;

    @Inject
    TestProcessor transactionProcessor;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private TransactionDao transactionDao;

    @Inject
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() throws Exception {
        users = testUtil.getTestUsers("UpdateTransactionsOnContextCommandTest");
        categories = categoryRepository.findAll();

        this.credentials = users.stream().map(user ->{
            return testUtil.getCredentials(user, "swedbank-bankid");
        }).collect(Collectors.toMap(c -> c.getUserId(), c -> c));

        Map<String, List<Transaction>> transactions = getTransactions(10, "test").stream()
                .collect(Collectors.groupingBy(Transaction::getUserId));
        contexts = users.stream().map(user ->{
            TransactionProcessorContext transactionProcessorContext = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions.get(user.getId())
            );
            TransactionProcessorUserData processorUserData = new TransactionProcessorUserData();
            processorUserData.setCredentials(Lists.newArrayList(credentials.get(user.getId())));

            transactionProcessorContext.setUserData(processorUserData);
            transactionProcessorContext.setCredentialsId(credentials.get(user.getId()).getId());
            return transactionProcessorContext;
        }).collect(Collectors.toMap(tpc -> tpc.getUser().getId(), tpc -> tpc));

        userDatas = users.stream().map(user -> {
            UserData userData = new UserData();
            userData.setUser(user);
            return userData;
        }).collect(Collectors.toMap(ud -> ud.getUser().getId(), ud -> ud));
    }

    @Test
    public void validate_transactionInStoreListIfCommandIsRun(){
        users.forEach(user -> {
            transactionProcessor.process(
                    contexts.get(user.getId()),
                    userDatas.get(user.getId()),
                    () -> new SimpleChainFactory(ctx -> ImmutableList.of(
                            new SaveTransactionCommand(ctx, transactionDao, metricRegistry),
                            new UpdateTransactionsOnContextCommand(ctx)
                    )),
                    true);

            Assert.assertEquals(10, contexts.get(user.getId()).getInBatchTransactions().size());
            Assert.assertEquals(10, contexts.get(user.getId()).getUserData().getInStoreTransactions().size());
        });
    }

    @Test
    public void validate_noTransactionInStoreListIfNotCommandIsRun(){
        users.forEach(user -> {
            transactionProcessor.process(
                    contexts.get(user.getId()),
                    userDatas.get(user.getId()),
                    () -> new SimpleChainFactory(
                            ctx -> ImmutableList.of(new SaveTransactionCommand(ctx, transactionDao, metricRegistry))),
                    true);

            Assert.assertEquals(10, contexts.get(user.getId()).getInBatchTransactions().size());
            Assert.assertEquals(0, contexts.get(user.getId()).getUserData().getInStoreTransactions().size());
        });

    }

    private List<Transaction> getTransactions(int n, String label){
        List<Transaction> transactions = Lists.newArrayList();
        users.forEach(user -> {
            for (int i = 0; i < n; i++) {
                Transaction transaction = testUtil.getNewTransaction(user.getId(), -i, label + i);
                transaction.setCategory(categories.get(i % categories.size()));
                transactions.add(transaction);
            }
        });

        return transactions;
    }

}

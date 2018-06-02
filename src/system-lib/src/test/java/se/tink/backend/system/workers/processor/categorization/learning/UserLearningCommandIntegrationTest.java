package se.tink.backend.system.workers.processor.categorization.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.util.EmbeddedEalsticSearch;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.metrics.MetricRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceRunner.class)
public class UserLearningCommandIntegrationTest{
    private Credentials credentials;
    private List<User> users;
    List<Category> categories;
    Map<String,List<Transaction>> transactions;
    private Map<String , UserData> userDatas;
    @Inject
    private TestUtil testUtil;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private CategoryRepository categoryRepository;

    @Rule
    public EmbeddedEalsticSearch embeddedEalsticSearch = new EmbeddedEalsticSearch();

    @Inject
    private AccountRepository accountRepository;

    @Inject
    private PostalCodeAreaRepository ostalCodeAreaRepository;

    @Inject
    private TestProcessor transactionProcessor;

    @Inject
    private TransactionDao transactionDao;

    @Inject
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() throws IOException {
        users = testUtil.getTestUsers("UserLearningCommandIntegrationTest");

        credentials = new Credentials();
        credentials.setProviderName("swedbank-bankid");
        categories = categoryRepository.findAll();
        Collections.sort(categories, ((o1, o2) -> o1.getSortOrder() - o2.getSortOrder()));

        transactions = getTestTransactions();
        userDatas = users.stream().map(user -> {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(Lists.newArrayList(credentials));
            return userData;
        }).collect(Collectors.toMap(ud -> ud.getUser().getId(), ud -> ud));

        List<Transaction> defaultTransactions = this.transactions.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Map<String, Category> categoriesById = categoryRepository.findAll().stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
        testUtil.index(embeddedEalsticSearch.getClient(), defaultTransactions, categoriesById);
    }

    private UserLearningCommand buildUserLearning(String userId, Collection<Transaction> inStoreTransactions) {
        return new UserLearningCommand(
                userId, new SimilarTransactionsSearcher(embeddedEalsticSearch.getClient(), accountRepository,
                ostalCodeAreaRepository, categoryRepository), new ClusterCategories(categories),
                inStoreTransactions
        );
    }

    @Test
    public void testEmptyDescription() {
        users.forEach( u -> {

            modifyCategory(transactions.get(u.getId()).get(0));
            saveTransaction(u, transactions.get(u.getId()));
            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "");
            transaction.setCredentialsId(credentials.getId());

            assertFalse(buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).isPresent());
        });

    }

    @Test
    public void testUserModifyCategory() {
        users.forEach( u -> {
            modifyCategory(transactions.get(u.getId()).get(0));
            saveTransaction(u, transactions.get(u.getId()));
            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "ICA MAXI");
            transaction.setCredentialsId(credentials.getId());
            modifyCategory(transaction);

            assertFalse(buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).isPresent());
        });
    }

    @Test
    public void testUserNoModifyCategories() {
        users.forEach( u -> {
            saveTransaction(u, transactions.get(u.getId()));
            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "ICA MAXI");
            transaction.setCredentialsId(credentials.getId());

            assertFalse(buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).isPresent());
        });
    }

    @Test
    public void testNotSimilarDescription() {
        users.forEach( u -> {
            modifyCategory(transactions.get(u.getId()).get(0));
            saveTransaction(u, transactions.get(u.getId()));
            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "test");
            transaction.setCredentialsId(credentials.getId());

            assertFalse(buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).isPresent());
        });
    }

    @Test
    public void testOneSimilarDescriptionUnmodified() {
        users.forEach( u -> {
            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "HAIR");
            transaction.setCredentialsId(credentials.getId());

            assertFalse(buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).isPresent());
        });
    }

    @Test
    public void testOneSimilarDescriptionModified() {
        users.forEach( u -> {
            modifyCategory(transactions.get(u.getId()).get(1));
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "HAIR");
            transaction.setCredentialsId(credentials.getId());

            Classifier.Outcome categorizationVectors = buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).get();
            assertEquals(categorizationVectors.command, CategorizationCommand.USER_LEARNING);
            assertTrue(categorizationVectors.vector.getDistribution().containsKey(categories.get(1).getCode()));
        });
    }

    @Test
    public void testTwoSimilarDescriptionModified() {
        users.forEach(u -> {
            modifyCategory(transactions.get(u.getId()).get(4));
            modifyCategory(transactions.get(u.getId()).get(9));
            transactions.get(u.getId()).get(9).setCategory(categories.get(19));
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "ICA");
            transaction.setCredentialsId(credentials.getId());

            assertFalse(buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).isPresent());
        });
    }

    @Test
    public void testTwoSimilarDescriptionModified2() {
        users.forEach( u -> {
            modifyCategory(transactions.get(u.getId()).get(4));
            modifyCategory(transactions.get(u.getId()).get(9));
            transactions.get(u.getId()).get(9).setCategory(categories.get(19));
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "ICA MATHORNAN");
            transaction.setCredentialsId(credentials.getId());

            Classifier.Outcome categorizationVectors = buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).get();
            assertEquals(categorizationVectors.command, CategorizationCommand.USER_LEARNING);
            assertTrue(categorizationVectors.vector.getDistribution().containsKey(categories.get(4).getCode()));
        });
    }

    @Test
    public void testTwoSimilarDescriptionOneModified() {
        users.forEach(u->{
            modifyCategory(transactions.get(u.getId()).get(4));
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "ICA");
            transaction.setCredentialsId(credentials.getId());

            Classifier.Outcome categorizationVectors = buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).get();
            assertEquals(categorizationVectors.command, CategorizationCommand.USER_LEARNING);
            assertTrue(categorizationVectors.vector.getDistribution().containsKey(categories.get(4).getCode()));
        });
    }

    @Test
    public void testTwoSimilarDescriptionAndSetUnmodifiedCategory() {
        users.forEach(u -> {
            modifyCategory(transactions.get(u.getId()).get(4));
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "ICA");
            transaction.setCredentialsId(credentials.getId());
            transaction.setCategory(transactions.get(u.getId()).get(9).getCategoryId(), transactions.get(u.getId()).get(9).getCategoryType());

            Classifier.Outcome categorizationVectors = buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).get();
            assertEquals(categorizationVectors.command, CategorizationCommand.USER_LEARNING);
            assertTrue(categorizationVectors.vector.getDistribution().containsKey(categories.get(4).getCode()));
        });
    }

    @Test
    public void testThreeSimilarDescriptionOneModified() {
        users.forEach( u ->{
            modifyCategory(transactions.get(u.getId()).get(10));
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "MAXI");
            transaction.setCredentialsId(credentials.getId());

            Classifier.Outcome categorizationVectors = buildUserLearning(u.getId(), transactions.get(u.getId())).categorize(transaction).get();
            assertEquals(categorizationVectors.command, CategorizationCommand.USER_LEARNING);
            assertTrue(categorizationVectors.vector.getDistribution().containsKey(categories.get(10).getCode()));
        });
    }

    @Test
    public void testThreeSimilarDescriptionUnmodifiedSameCategory() {
        users.forEach(u ->{
            modifyCategory(transactions.get(u.getId()).get(10));
            transactions.get(u.getId()).get(4).setCategory(transactions.get(u.getId()).get(8).getCategoryId(), transactions.get(u.getId()).get(8).getCategoryType());
            saveTransaction(u, transactions.get(u.getId()));

            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "MAXI");
            transaction.setCredentialsId(credentials.getId());

            assertTrue(u.getEndpoint(), buildUserLearning(u.getId(),transactions.get(u.getId())).categorize(transaction).isPresent());
        });
    }

    @Test
    public void testTwoSameThreeSimilarDescriptionDifferentCategories() {
        users.forEach( u -> {
            String[] nameSuffix = { "aaa", "bbb", "ccc" };
            modifyCategory(transactions.get(u.getId()).get(4));
            transactions.get(u.getId()).get(4).setDescription("Uber Trip " + nameSuffix[0]);

            modifyCategory(transactions.get(u.getId()).get(1));
            transactions.get(u.getId()).get(1).setCategory(transactions.get(u.getId()).get(4).getCategoryId(), transactions.get(u.getId()).get(4).getCategoryType());
            transactions.get(u.getId()).get(1).setDescription("Uber Trip " + nameSuffix[1]);

            modifyCategory(transactions.get(u.getId()).get(6));
            transactions.get(u.getId()).get(6).setCategory(transactions.get(u.getId()).get(4).getCategoryId(), transactions.get(u.getId()).get(4).getCategoryType());
            transactions.get(u.getId()).get(6).setDescription("Uber Trip " + nameSuffix[2]);

            // Eats
            modifyCategory(transactions.get(u.getId()).get(9));
            transactions.get(u.getId()).get(9).setCategory(transactions.get(u.getId()).get(9).getCategoryId(), transactions.get(u.getId()).get(9).getCategoryType());
            transactions.get(u.getId()).get(9).setDescription("Uber Eats");

            modifyCategory(transactions.get(u.getId()).get(10));
            transactions.get(u.getId()).get(10).setCategory(transactions.get(u.getId()).get(9).getCategoryId(), transactions.get(u.getId()).get(9).getCategoryType());
            transactions.get(u.getId()).get(10).setDescription("Uber Eats");

            saveTransaction(u, transactions.get(u.getId()));
            Transaction transaction = testUtil.getNewTransaction(u.getId(), -99, "Uber Eats");
            transaction.setCredentialsId(credentials.getId());

            Optional<Classifier.Outcome> optionalCategorizationVectors = buildUserLearning(u.getId(), transactions.get(u.getId()))
                    .categorize(transaction);

            assertTrue(optionalCategorizationVectors.isPresent());
            assertEquals(optionalCategorizationVectors.get().command, CategorizationCommand.USER_LEARNING);
            assertTrue(
                    optionalCategorizationVectors.get().vector.getDistribution().containsKey(categories.get(9).getCode()));
        });
    }

    private void saveTransaction(User user, List<Transaction> transactions) {
        UserData userData = userDatas.get(user.getId());
        userData.setTransactions(Lists.newArrayList());

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                transactions,
                userData,
                credentials.getId()
        );

        transactionProcessor.process(
                context,
                userData,
                () -> new SimpleChainFactory( ctx -> ImmutableList
                                .of(new SaveTransactionCommand(ctx, transactionDao, metricRegistry))),
                true);


        Map<String, Category> categoriesById = categoryRepository.findAll().stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
        testUtil.index(embeddedEalsticSearch.getClient(), transactions, categoriesById);
    }

    private Map<String,List<Transaction>> getTestTransactions() {
         return users.stream().collect(Collectors.<User, String, List<Transaction>>toMap(u -> u.getId(), u -> {
            List<Transaction> transactions = testUtil.getTestTransactions(u.getId());
            int i = 0;
            for (Transaction transaction : transactions) {
                transaction.setCategory(categories.get(i++ % categories.size()));
                transaction.setCredentialsId(credentials.getId());
            }
            return transactions;
        }));
    }

    private void modifyCategory(Transaction transaction) {
        transaction.setUserModifiedCategory(true);
    }
}

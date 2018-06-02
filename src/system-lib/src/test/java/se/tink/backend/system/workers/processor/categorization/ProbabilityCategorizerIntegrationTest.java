package se.tink.backend.system.workers.processor.categorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.ProbabilityCategorizer;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.system.workers.processor.storage.LoadUserDataCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.backend.util.EmbeddedEalsticSearch;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static se.tink.backend.core.CategorizationCommand.GENERAL_EXPENSES;
import static se.tink.backend.core.CategorizationCommand.GLOBAL_RULES;

@RunWith(GuiceRunner.class)
public class ProbabilityCategorizerIntegrationTest {
    public static final String PROBABILITY_CATEGORIZER_TEST_LABEL = "probability-categorizer-test";
    // TODO: Make this ClusterCategories.
    private List<Category> categories;

    private Map<String, Credentials> credentials;
    private List<User> users;
    private String uncategorizedExpenses;
    private String uncategorizedIncome;
    private String servicesCategory;
    private String coffeeCategory;
    private String rentCategory;
    private String foodOtherCategory;
    private String transferUnknownCategory;

    protected Counter newTransactionMeter;
    protected Counter noNewTransactionsMeter;
    protected Counter positiveIncomingTransactionMeter;
    protected Counter negativeIncomingTransactionMeter;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Category> categoriesById;
    private Map<String, Category> categoriesByCode;

    @Rule
    public EmbeddedEalsticSearch embeddedEalsticSearch = new EmbeddedEalsticSearch();

    @Inject
    private TestUtil testUtil;

    @Inject
    private CategoryConfiguration categoryConfiguration;

    @Inject
    private AccountRepository accountRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private CredentialsRepository credentialsRepository;

    @Inject
    private LoanDataRepository loanDataRepository;

    @Inject
    private TransactionDao transactionDao;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private CategoryChangeRecordDao categoryChangeRecordDao;

    @Inject
    private PostalCodeAreaRepository postalCodeAreaRepository;

    @Inject
    private TestProcessor transactionProcessor;

    @Before
    public void setUp() {
        categories = categoryRepository.findAll();
        users = testUtil.getTestUsers("ProbabilityCategorizerIntegrationTestUser");
        uncategorizedExpenses = categoryConfiguration.getExpenseUnknownCode();
        uncategorizedIncome = categoryConfiguration.getIncomeUnknownCode();
        servicesCategory = categoryConfiguration.getServicesCode();
        coffeeCategory = categoryConfiguration.getCoffeeCode();
        rentCategory = categoryConfiguration.getRentCode();
        foodOtherCategory = categoryConfiguration.getFoodOtherCode();
        transferUnknownCategory = categoryConfiguration.getTransferUnknownCode();
        credentials= users.stream()
                .map( user -> testUtil.getCredentials(user, "swedbank-bankid")).collect(Collectors.toMap(c-> c.getUserId(), c -> c));
        categoriesById = categories.stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
        categoriesByCode= categories.stream().collect(Collectors.toMap(c -> c.getCode(), c -> c));
        positiveIncomingTransactionMeter = (Counter)metricRegistry.get(TransactionProcessor.TRANSACTIONS_INCOMING_POSITIVE);
        negativeIncomingTransactionMeter = (Counter)metricRegistry.get(TransactionProcessor.TRANSACTIONS_INCOMING_NEGATIVE);
        noNewTransactionsMeter = (Counter)metricRegistry.get(TransactionProcessor.TRANSACTIONS_NO_NEW);
        newTransactionMeter = (Counter)metricRegistry.get(TransactionProcessor.TRANSACTIONS_NEW);
    }

    private static class InjectedVector implements Classifier {

        private final CategorizationCommand command;
        private final CategorizationVector vector;

        private InjectedVector(CategorizationCommand command, CategorizationVector vector) {
            this.command = command;
            this.vector = vector;
        }

        @Override
        public Optional<Outcome> categorize(Transaction transaction) {
            return Optional.of(new Outcome(command, vector));
        }
    }

    private Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> buildDefaultTestChain(
            Classifier additionalClassifier) {
        return buildDefaultTestChain(Optional.of(additionalClassifier));
    }

    private Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> buildDefaultTestChain() {
        return buildDefaultTestChain(Optional.empty());
    }

    private Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> buildDefaultTestChain(
            Optional<Classifier> additionalClassifier) {
        return context -> {
            ImmutableList.Builder<Classifier> builder = ImmutableList.builder();

            additionalClassifier.ifPresent(builder::add);
            builder.add(
                    new UserLearningCommand(
                            context.getUser().getId(),
                            new SimilarTransactionsSearcher(embeddedEalsticSearch.getClient(), accountRepository,
                                    postalCodeAreaRepository, categoryRepository),
                            new ClusterCategories(categories),
                            context.getUserData().getInStoreTransactions().values()
                    )
            );

            return ImmutableList.of(
                    new LoadUserDataCommand(
                            context, credentialsRepository, loanDataRepository, transactionDao,
                            accountRepository),
                    new CategorizerCommand(
                            new ProbabilityCategorizer(
                                    context.getUser(),
                                    categoryConfiguration,
                                    metricRegistry,
                                    new ClusterCategories(categories),
                                    builder.build(),
                                    PROBABILITY_CATEGORIZER_TEST_LABEL), categoryChangeRecordDao
                    )
            );
        };
    }

    private ImmutableList<TransactionProcessorCommand> buildTransferTestChain(
            TransactionProcessorContext context) {
        return ImmutableList.of(
                new LoadUserDataCommand(
                        context, credentialsRepository, loanDataRepository, transactionDao, accountRepository),
                new TransferDetectionCommand(
                        context, categoryConfiguration,
                        TransferDetectionScorerFactory.byCluster(Cluster.TINK),
                        new ClusterCategories(categories), categoryChangeRecordDao
                ),
                new CategorizerCommand(
                        new ProbabilityCategorizer(
                                context.getUser(),
                                categoryConfiguration,
                                metricRegistry,
                                new ClusterCategories(categories),
                                ImmutableList.of(
                                        new UserLearningCommand(
                                                context.getUser().getId(),
                                                new SimilarTransactionsSearcher(embeddedEalsticSearch.getClient(), accountRepository,
                                                        postalCodeAreaRepository, categoryRepository),
                                                new ClusterCategories(categories),
                                                context.getUserData().getInStoreTransactions().values()
                                        )
                                ),
                                PROBABILITY_CATEGORIZER_TEST_LABEL), categoryChangeRecordDao
                )
        );
    }

    @Test
    public void testTransferCategoryChangedByUserLearning() {
        users.forEach(user -> {
            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = this::buildTransferTestChain;
            // old transaction to be used in user learning
            List<Transaction> transactions = getTestTransactions(user.getId());
            Transaction oldTransaction = transactions.get(1);
            oldTransaction.setUserModifiedCategory(true);
            saveTransaction(transactions, user);

            // twin transaction is present in transactions to trigger transfer detection
            // matches the description of oldCategory partially
            Transaction transaction = testUtil.getNewTransaction(user.getId(), -400, "HAIR");
            transaction.setAccountId("newAccountId");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());
            transaction.setCategory(categories.get(0));

            processTransactionAndAssertMeters(null, transaction, transactions, 1, 1, 0, commandsBuilder, user);

            assertCategoriesEquals(categories, oldTransaction.getCategoryId(), transaction.getCategoryId());
            assertEquals(oldTransaction.getCategoryType(), transaction.getCategoryType());
        });
    }

    @Test
    public void testTransferCategoryWithoutMatchingUserLearning() {
        when(categoryRepository.findByCode(SECategories.Codes.TRANSFERS_OTHER_OTHER)).thenReturn(categoriesByCode.get(SECategories.Codes.TRANSFERS_OTHER_OTHER));
        // old transaction to be used in user learning
        users.forEach(user -> {

            List<Transaction> transactions = getTestTransactions(user.getId());
            Transaction oldTransaction = transactions.get(1);
            oldTransaction.setUserModifiedCategory(true);
            saveTransaction(transactions, user);

            // no transaction matches the description
            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "unfamiliar description");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            Category category = categoryRepository.findByCode(transferUnknownCategory);

            transaction.setCategory(category);

            // some other than user learning categorization heuristics present
            TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    singletonList(transaction)
            );
            Category expenseCategory = categories.get(3);
            CategorizationVector categorizationVector = new CategorizationVector(1, expenseCategory.getId(), 1);

            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder =
                    buildDefaultTestChain(new InjectedVector(GENERAL_EXPENSES, categorizationVector));
            processTransactionAndAssertMeters(context, transaction, transactions, 1, 1, 0, commandsBuilder, user);

            assertCategoriesEquals(categories, category.getId(), transaction.getCategoryId());
            assertEquals(category.getType(), transaction.getCategoryType());
        });
    }

    @Test
    public void testTransferCategoryWithoutUserModifiedTransactions() {
        Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = buildDefaultTestChain();
        users.forEach(user -> {
            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "HAIR");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            Optional<Category> transferCategory = categories.stream()
                    .filter(c -> c.getCode().equals(transferUnknownCategory))
                    .findFirst();

            Assert.assertTrue(transferCategory.isPresent());

            transaction.setCategory(transferCategory.get());

            processTransactionAndAssertMeters(null, transaction, Collections.emptyList(), 1,
                    1, 0, commandsBuilder, user);

            assertCategoriesEquals(categories, transferCategory.get().getId(), transaction.getCategoryId());
            assertEquals(transferCategory.get().getType(), transaction.getCategoryType());
        });
    }

    @Test
    public void testUnknownIncome() {
        users.forEach(user -> {
            Transaction transaction = testUtil.getNewTransaction(user.getId(), 99, "income");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            processTransactionAndAssertMeters(null, transaction,
                    Collections.emptyList(), 1, 1, 0,
                    buildDefaultTestChain(), user);

            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, getCategoryByCode(uncategorizedIncome).getId(), transaction.getCategoryId());
            assertEquals(CategoryTypes.INCOME, transaction.getCategoryType());
        });
    }

    @Test
    public void testUnknownExpense() {
        users.forEach(user -> {
            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "expense");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            processTransactionAndAssertMeters(null, transaction,
                    Collections.emptyList(), 1, 1, 0,
                    buildDefaultTestChain(), user);

            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, getCategoryByCode(uncategorizedExpenses).getId(),
                    transaction.getCategoryId());
            assertEquals(CategoryTypes.EXPENSES, transaction.getCategoryType());
        });
    }

    private Category getCategoryByCode(String code) {
        return categories.stream().filter(c -> code.equals(c.getCode())).findFirst().get();
    }

    @Test
    public void testOneProbableCategory() {
        users.forEach(user -> {
            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = buildDefaultTestChain();

            int modTransIndex = 1;
            List<Transaction> transactions = getTestTransactions(user.getId());
            setCategoryIfMissingAsUser(transactions.get(modTransIndex), categories.get(0));
            saveTransaction(transactions, user);

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "HAIR");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            processTransactionAndAssertMeters(null, transaction, transactions, 1, 1, 0, commandsBuilder, user);
            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, transactions.get(modTransIndex).getCategoryId(),
                    transaction.getCategoryId());
            assertEquals(transactions.get(modTransIndex).getCategoryType(), transaction.getCategoryType());
        });
    }

    @Test
    public void testOneProbableCategoryWithCategory() {
        users.forEach(user -> {
            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = buildDefaultTestChain();

            int modTransIndex = 1;
            List<Transaction> transactions = getTestTransactions(user.getId());
            setCategoryIfMissingAsUser(transactions.get(modTransIndex), categories.get(0));
            saveTransaction(transactions, user);

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "HAIR");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());
            transaction.setCategory(categories.get(0));

            processTransactionAndAssertMeters(null, transaction, transactions, 1, 1, 0, commandsBuilder, user);
            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, transactions.get(modTransIndex).getCategoryId(),
                    transaction.getCategoryId());
            assertEquals(transactions.get(modTransIndex).getCategoryType(), transaction.getCategoryType());
        });
    }

    @Test
    public void testTwoProbableCategory() {
        Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = buildDefaultTestChain();
        users.forEach(user -> {
            int modTransIndex = 4;
            List<Transaction> transactions = getTestTransactions(user.getId());
            setCategoryIfMissingAsUser(transactions.get(modTransIndex), categories.get(0));
            setCategoryIfMissingAsUser(transactions.get(9), categories.get(0));
            saveTransaction(transactions, user);

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "ICA MATHORNAN");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            processTransactionAndAssertMeters(null, transaction, transactions, 1, 1, 0, commandsBuilder, user);
            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, transactions.get(modTransIndex).getCategoryId(),
                    transaction.getCategoryId());
            assertEquals(transactions.get(modTransIndex).getCategoryType(), transaction.getCategoryType());
        });
    }

    @Test
    @Ignore
    public void testTwoMostProbableCategorySame() {
        users.forEach(user -> {
            List<Transaction> transactions = getTestTransactions(user.getId());
            transactions.get(3).setCategory(categoryRepository.findByCode(rentCategory));
            setCategoryIfMissingAsUser(transactions.get(3), categories.get(0));
            saveTransaction(transactions, user);

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "URBAN CAFE");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = buildDefaultTestChain(
                    new InjectedVector(GLOBAL_RULES, new CategorizationVector(10, coffeeCategory, 1)));
            processTransactionAndAssertMeters(createContext(transaction, user), transaction, transactions, 1, 1,
                    0,
                    commandsBuilder, user);

            Category expCategory = categoryRepository.findByCode(foodOtherCategory);

            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, expCategory.getId(), transaction.getCategoryId());
            assertEquals(expCategory.getType(), transaction.getCategoryType());
        });
    }

    @Test
    @Ignore
    public void testTwoMostProbableCategorySame2() {
        users.forEach(user -> {
            List<Transaction> transactions = getTestTransactions(user.getId());
            transactions.get(3).setCategory(categoryRepository.findByCode(rentCategory));
            setCategoryIfMissingAsUser(transactions.get(3), categories.get(0));
            saveTransaction(transactions, user);

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "URBAN CAFE");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder = buildDefaultTestChain(
                    new InjectedVector(GLOBAL_RULES, new CategorizationVector(10, servicesCategory, 1)));
            processTransactionAndAssertMeters(createContext(transaction, user), transaction, transactions, 1, 1,
                    0,
                    commandsBuilder, user);

            Category expCategory = categoryRepository.findByCode(uncategorizedExpenses);

            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, expCategory.getId(), transaction.getCategoryId());
            assertEquals(expCategory.getType(), transaction.getCategoryType());
        });
    }

    @Test
    public void testTwoMostProbableCategoryNotSame() {
        when(categoryRepository.findByCode(SECategories.Codes.EXPENSES_HOME_RENT)).thenReturn(categoriesByCode.get(SECategories.Codes.EXPENSES_HOME_RENT));
        users.forEach(user -> {
            List<Transaction> transactions = getTestTransactions(user.getId());
            transactions.get(3).setCategory(categoryRepository.findByCode(rentCategory));
            transactions.get(3).setUserModifiedCategory(true);
            saveTransaction(transactions, user);

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -99, "URBAN CAFE");
            transaction.setCredentialsId(credentials.get(user.getId()).getId());

            processTransactionAndAssertMeters(
                    createContext(transaction, user), transaction, transactions, 1, 1, 0,
                    buildDefaultTestChain(
                            new InjectedVector(GLOBAL_RULES, new CategorizationVector(10, coffeeCategory, 1))), user);

            assertNotNull(transaction.getCategoryId());
            assertCategoriesEquals(categories, getCategoryByCode(uncategorizedExpenses).getId(),
                    transaction.getCategoryId());
            assertEquals(CategoryTypes.EXPENSES, transaction.getCategoryType());
        });
    }

    private void processTransactions(Transaction newTransaction,
                                     List<Transaction> inStoreTransactions, TransactionProcessorContext context,
                                     Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder, User user) {
        List<Transaction> transactions = singletonList(newTransaction);

        if (context == null) {
            context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions
            );
        }
        Preconditions.checkNotNull(context.getUserData().getInStoreTransactions());
        context.getUserData().setInStoreTransactions(inStoreTransactions);

        UserData userData = new UserData();
        userData.setUser(user);
        userData.setTransactions(transactions);

        transactionProcessor.process(context, userData, () -> new SimpleChainFactory(commandsBuilder),
                true);
        testUtil.index(embeddedEalsticSearch.getClient(), transactions, categoriesById);
    }

    private TransactionProcessorContext createContext(Transaction transaction, User user) {
        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                singletonList(transaction)
        );

        return context;
    }

    private void processTransactionAndAssertMeters(TransactionProcessorContext context,
                                                   Transaction newTransaction, List<Transaction> inStoreTransactions,
                                                   long expIncomeTransCount,
                                                   long expNewTransCount, long expNoNewTransCount,
                                                   Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commandsBuilder, User user) {
        long incomeTransCount =
                positiveIncomingTransactionMeter.getCount() + negativeIncomingTransactionMeter.getCount();
        long newTransCount = newTransactionMeter.getCount();
        long noNewTransCount = noNewTransactionsMeter.getCount();

        processTransactions(newTransaction, inStoreTransactions, context, commandsBuilder, user);

        long actIncomeTransCount =
                positiveIncomingTransactionMeter.getCount() + negativeIncomingTransactionMeter.getCount()
                        - incomeTransCount;
        long actNewTransCount = newTransactionMeter.getCount() - newTransCount;
        long actNoNewTransCount = noNewTransactionsMeter.getCount() - noNewTransCount;

        assertEquals("Not same count of income transactions", expIncomeTransCount, actIncomeTransCount);
        assertEquals("Not same count of new transactions", expNewTransCount, actNewTransCount);
        assertEquals("Not same count of no new transactions", expNoNewTransCount, actNoNewTransCount);
    }

    private void saveTransaction(List<Transaction> transactions, User user) {
        UserData userData = new UserData();
        userData.setUser(user);
        userData.setCredentials(Lists.newArrayList(credentials.get(user.getId())));
        userData.setTransactions(Lists.newArrayList());

        int row = 0;
        System.out.println("Saving transactions:");
        for (Transaction transaction : transactions) {
            System.out.println(String.format(" * %d: %s %s", row++, transaction.getDescription(),
                    categoryRepository.findById(transaction.getCategoryId()).getCode()));
        }

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                transactions,
                userData,
                credentials.get(user.getId()).getId()
        );

        Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> commands = ctx -> ImmutableList
                .of(new SaveTransactionCommand(ctx, transactionDao, metricRegistry));

        transactionProcessor.process(context, userData, () -> new SimpleChainFactory(commands),
                true);

        testUtil.index(embeddedEalsticSearch.getClient(), transactions, categoriesById);
    }

    public List<Transaction> getTestTransactions(String userId) {
        List<Transaction> transactions = testUtil.getTestTransactions(userId);

        int i = 0;
        for (Transaction transaction : transactions) {
            // TODO: Make order of categories deterministic to make the tests deterministic.
            Category category = categories.get(i++ % categories.size());
            while (category.getType() != CategoryTypes.EXPENSES) {
                category = categories.get(i++ % categories.size());
            }
            transaction.setCategory(category);
            transaction.setCredentialsId(credentials.get(userId).getId());
        }

        return transactions;
    }

    private void setCategoryIfMissingAsUser(Transaction transaction, Category categoryToChangeTo) {
        if (transaction.getCategoryId() == null) {
            transaction.setCategory(categoryToChangeTo);
        }

        transaction.setUserModifiedCategory(true);
    }

    // Nicer to compare codes than IDs.
    private void assertCategoriesEquals(List<Category> categories, final String categoryId1, final String categoryId2) {
        Category category1 = categories.stream().filter(c -> c.getId().equals(categoryId1)).findFirst().get();
        Category category2 = categories.stream().filter(c -> c.getId().equals(categoryId2)).findFirst().get();

        Assert.assertEquals(category1.getCode(), category2.getCode());
    }

}

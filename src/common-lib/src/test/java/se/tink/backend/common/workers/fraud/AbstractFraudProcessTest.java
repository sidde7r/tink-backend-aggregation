package se.tink.backend.common.workers.fraud;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.location.LocationTestUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudCompanyEngagementContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.FraudNonPaymentContent;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public abstract class AbstractFraudProcessTest {
    protected static final String noFrequentAccountId = "noFrequentAccountId";
    protected static final String frequentAccountId = "frequentAccountId";
    protected static final Date today = new Date();
    protected FraudDataProcessorContext processorContext;

    protected User user;
    protected Map<String, Transaction> transactionMap;
    protected Category withdrawalCategory;

    @Before
    public void preSetUp() throws Exception {
        user = new User();
        user.setId("userId");

        transactionMap = Maps.newHashMap();

        processorContext = new FraudDataProcessorContext();
        processorContext.setUser(user);
        processorContext.setCategoryConfiguration(new SECategories());
        processorContext.setCategoriesByCodeForLocale(setupCategories());
        processorContext.setUserCurrency(setupCurrency());
        processorContext.setAccountsById(createAccounts());
        processorContext.setTransactionsById(transactionMap);
        processorContext.setActivities(Lists.<Activity>newArrayList());
        processorContext.setInStoreFraudItems(FraudUtils.createBasicFraudItems(new User()));

        Provider provider = new Provider();
        provider.setDisplayName("PROVIDER");
        processorContext.setProviders(Lists.newArrayList(provider));
    }

    protected void addDataToRunEveryProcessor() throws Exception {

        Transaction transaction = createTransaction("", "accountId", today, -1000, "categoryId");
        transaction.setDescription("transaction");

        Activity activity = new Activity();
        activity.setType(Activity.Types.LARGE_EXPENSE);
        activity.setDate(today);
        activity.setContent(transaction);

        processorContext.getActivities().add(activity);

        transactionMap.putAll(createTransactions(DateUtils.addMonths(today, -6),
                DateUtils.addDays(today, -30), frequentAccountId, noFrequentAccountId));

        addTransaction("account1", today, -600, withdrawalCategory.getId());
    }

    protected void mockupTransactions(Collection<Transaction> collection) {
        Map<String, Transaction> transactionsById = Maps.uniqueIndex(collection, Transaction::getId);
        processorContext.setTransactionsById(transactionsById);
    }

    protected Transaction createTransaction(String description, String accountId, Date date, double amount,
            String categoryId) {
        Category category = new Category();
        category.setType(CategoryTypes.EXPENSES);
        category.setId(categoryId);

        return TestUtils.createTransaction(description, amount, date, processorContext.getUser().getId(), category, accountId);
    }

    protected String addTransaction(String accountId, Date date, double amount, String categoryId) {
        Transaction t = createTransaction("", accountId, date, -amount, categoryId);
        transactionMap.put(t.getId(), t);
        return t.getId();
    }

    protected Map<String, Transaction> createTransactions(Date first, Date last, String frequentAccountId,
            String noFrequentAccountId) {
        Random r = new Random();

        List<Transaction> transactions = Lists.newArrayList();

        int days = DateUtils.daysBetween(first, last);

        for (int i = 0; i <= days; i++) {
            Date curr = DateUtils.addDays(last, -i);
            String id = r.nextDouble() > 0.9 ? noFrequentAccountId : frequentAccountId;

            int max = r.nextInt(5);
            for (int j = 0; j < max; j++) {
                transactions.add(createTransaction("", id, curr, -70, "categoryId"));
            }
        }

        Map<String, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {
            map.put(t.getId(), t);
        }
        return map;
    }

    protected List<Transaction> createTransactionsNoFraudulantMerchant(int count) throws Exception {
        List<Transaction> transactions = Lists.newArrayList();
        Date today = new Date();
        for (int i = 0; i < count; i++) {
            Transaction t = LocationTestUtils.createTransaction(ThreadSafeDateFormat.FORMATTER_MINUTES.format(
                    DateUtils.addDays(today, -i)), "merchantStockholm1");

            if (i == 1) {
                t.setPayload(TransactionPayloadTypes.GIRO, "dafksdasfa");
            }

            transactions.add(t);
        }
        return transactions;
    }

    protected List<Transaction> createTransactionsLargeWithdrawal(int count) throws Exception {
        final Category withdrawalCategory = processorContext.getCategoriesByCodeForLocale()
                .get(processorContext.getCategoryConfiguration().getWithdrawalsCode());
        List<Transaction> transactions = Lists.newArrayList();
        Date today = new Date();
        for (int i = 0; i < count; i++) {
            Transaction t = LocationTestUtils.createTransaction(ThreadSafeDateFormat.FORMATTER_MINUTES.format(
                    DateUtils.addDays(today, -i)), "merchantStockholm1");

            if (i == 1) {
                t.setAmount(-20000.);
                t.setCategory(withdrawalCategory);
                t.setDescription("Large Withdrawal");
            } else {
                t.setAmount(-20.);
                t.setDescription("Not Large Withdrawal");
            }

            transactions.add(t);

        }
        return transactions;
    }

    protected FraudDetails createFraudDetails(FraudDetailsContent detailsContent) {
        return createFraudDetails(detailsContent, FraudStatus.OK);
    }

    protected FraudDetails createFraudDetails(FraudDetailsContent fraudDetailsContent,
            FraudStatus status) {
        FraudDetails details = new FraudDetails();
        details.setContent(fraudDetailsContent);
        details.setType(fraudDetailsContent.getContentType());
        details.setStatus(status);
        details.setDate(today);

        return details;
    }

    protected List<FraudDetails> createFraudDetails(List<FraudDetailsContent> detailsContents) {
        List<FraudDetails> fraudDetailses = Lists.newArrayList();

        for (FraudDetailsContent detailsContent : detailsContents) {
            fraudDetailses.add(createFraudDetails(detailsContent));
        }

        return fraudDetailses;
    }

    protected void addInStoreFraudDetails(FraudDetails... fraudDetails) {
        addInStoreFraudDetails(Arrays.asList(fraudDetails));
    }

    protected void addInStoreFraudDetails(List<FraudDetails> fraudDetailsList) {

        if (processorContext.getInStoreFraudDetails() == null) {
            processorContext.setInStoreFraudDetails(Lists.<FraudDetails>newArrayList());
        }

        processorContext.getInStoreFraudDetails().addAll(fraudDetailsList);
    }

    protected void addFraudDetailsContent(List<FraudDetailsContent> fraudDetailsContents) {
        processorContext.addFraudDetailsContent(fraudDetailsContents);
    }

    protected List<FraudDetailsContent> createTestDetailsContents() {
        FraudIdentityContent identityContent = new FraudIdentityContent();
        identityContent.setPersonIdentityNumber("201212121212");
        identityContent.setFirstName("Ben");
        identityContent.setLastName("Kingsley");
        identityContent.setContentType(FraudDetailsContentType.IDENTITY);

        FraudAddressContent addressContent = new FraudAddressContent();
        addressContent.setAddress("Wallingatan 5");
        addressContent.setCity("Stockholm");
        addressContent.setContentType(FraudDetailsContentType.ADDRESS);

        FraudNonPaymentContent nonPaymentContent = new FraudNonPaymentContent();
        nonPaymentContent.setAmount(500.);
        nonPaymentContent.setType("Type");
        nonPaymentContent.setContentType(FraudDetailsContentType.NON_PAYMENT);

        FraudCompanyEngagementContent companyEngagementContent = new FraudCompanyEngagementContent();
        companyEngagementContent.setContentType(FraudDetailsContentType.COMPANY_ENGAGEMENT);

        FraudIncomeContent incomeContent = new FraudIncomeContent();
        incomeContent.setFinalTax(12345);
        incomeContent.setTotalIncome(123456);
        incomeContent.setContentType(FraudDetailsContentType.INCOME);

        return Lists.newArrayList(identityContent, addressContent, nonPaymentContent, companyEngagementContent,
                incomeContent);
    }

    protected FraudTransactionContent createFraudTransactionContent(FraudDetailsContentType type,
            Transaction... transactions) {
        return createFraudTransactionContent(type, Arrays.asList(transactions));
    }

    protected FraudTransactionContent createFraudTransactionContent(FraudDetailsContentType type,
            List<Transaction> transactions) {
        FraudTransactionContent content = new FraudTransactionContent();
        content.setContentType(type);

        List<FraudTransactionEntity> transactionEntities = Lists.newArrayList();
        for (Transaction transaction : transactions) {
            FraudTransactionEntity transactionEntity = new FraudTransactionEntity(transaction);
            transactionEntity.setDescription(transaction.getDescription());
            transactionEntities.add(transactionEntity);
        }

        content.setTransactions(transactionEntities);
        content.setTransactionIds(
                Lists.newArrayList(Iterables.transform(transactions, Transaction::getId)));

        return content;
    }

    private Map<String, Account> createAccounts() {
        Account a1 = new Account();
        a1.setId(noFrequentAccountId);
        Account a2 = new Account();
        a2.setId(frequentAccountId);
        Map<String, Account> accounts = Maps.newHashMap();
        accounts.put(noFrequentAccountId, a1);
        accounts.put(frequentAccountId, a2);
        return accounts;
    }

    private Currency setupCurrency() {
        Currency c = new Currency();
        c.setCode("SEK");
        c.setFactor(10);
        return c;
    }

    private Map<String, Category> setupCategories() {
        withdrawalCategory = new Category();
        withdrawalCategory.setCode(processorContext.getCategoryConfiguration().getWithdrawalsCode());
        withdrawalCategory.setType(CategoryTypes.EXPENSES);
        Map<String, Category> categoryMap = Maps.newHashMap();
        categoryMap.put(processorContext.getCategoryConfiguration().getWithdrawalsCode(), withdrawalCategory);
        return categoryMap;
    }
}

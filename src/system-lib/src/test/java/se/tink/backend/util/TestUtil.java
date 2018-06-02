package se.tink.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.util.Collections;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.joda.time.LocalDate;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.uuid.UUIDUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestUtil {
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private static final String STATIC_ACCOUNT_ID = UUIDUtils.toTinkUUID(UUID.randomUUID());
    private SECategories categoryConfiguration = new SECategories();
    private ProviderDao providerDao;
    private ObjectMapper objectMapper;

    @Inject
    public TestUtil(UserRepository userespository, AccountRepository accountRespository, CategoryRepository categoryRepository, ProviderDao providerDao) {
        this.userRepository = userespository;
        this.accountRepository = accountRespository;
        this.categoryRepository = categoryRepository;
        this.providerDao = providerDao;
        this.objectMapper = new ObjectMapper();
    }

    public ImmutableMap<String, Provider> getProvidersByName() {
        return providerDao.getProvidersByName();
    }

    public List<User> getTestUsers(String testName) {
        return getTestUserWithFeatures(testName, Collections.emptyList());
    }

    public void rebuildTransactionIndex(Client client) {
        try {
            client.admin().indices().delete(new DeleteIndexRequest("transactions")).actionGet();
        } catch (Exception e) {
            // NOOP.
        }

        String settings = null;
        String transactionMappings = null;
        try {
            settings = Files.toString(new File("data/search/search-settings-transaction.json"), Charsets.UTF_8);
            transactionMappings = Files.toString(new File("data/search/search-mappings-transaction.json"),
                    Charsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }

        client.admin().indices().prepareCreate("transactions").setSettings(settings)
                .addMapping("transaction", transactionMappings).execute().actionGet();

    }

    public void index(Client client, List<Transaction> transactions, Map<String, Category> categoriesById) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        transactions.forEach(transaction -> {
            Optional<Category> categoryOptional = Optional.ofNullable(categoriesById.get(transaction.getCategoryId()));
            Category category = clearCategorySearchTerms(categoryOptional);
            // Fields marked with JsonIgnore will not be included in merchant index
            String content = null;
            try {
                content = objectMapper.writeValueAsString(new TransactionSearchContainer(
                        transaction, category));
            } catch (JsonProcessingException e) {
                //NOOP
            }
            bulkRequest.add(client.prepareIndex("transactions", "transaction", transaction.getId())
                    .setSource(content).setRouting(transaction.getUserId()));
        });
        bulkRequest.setRefresh(true);
        bulkRequest.execute().actionGet();
    }

    private Category clearCategorySearchTerms(Optional<Category> category) {
        return category
                .map(c -> {
                    c.setSearchTerms(null);
                    return c;
                })
                .orElse(null);
    }



    public User getTestUser(String testName) {
        UserProfile profile = new UserProfile();
        profile.setCurrency("SEK");
        profile.setPeriodAdjustedDay(25);
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setLocale("sv_SE");
        profile.setMarket("SE");

        User user = new User();
        user.setId(StringUtils.generateUUID());
        user.setPassword(testName);
        user.setUsername(testName);
        user.setProfile(profile);
        user.setFlags(Collections.emptyList());
        return user;
    }

    public List<User> getTestUserWithFeatures(String testName, List<String> flags) {
        List<User> users = ImmutableList.of(flags)
                .stream()
                .map(f -> {
                    User user;
                    String userName;
                    if (f.isEmpty()){
                        userName = testName;
                        user = userRepository.findOneByUsername(userName);
                    }
                    else{
                        userName = testName + ".flagged";
                        user = userRepository.findOneByUsername(userName);
                    }

                    if (user == null) {
                        user = new User();
                        user.setId(StringUtils.generateUUID());
                        user.setPassword(userName);
                        user.setUsername(userName);
                        user.setFlags(f);
                        UserProfile profile = new UserProfile();
                        profile.setCurrency("SEK");
                        profile.setPeriodAdjustedDay(25);
                        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
                        profile.setLocale("sv_SE");
                        profile.setMarket("SE");
                        user.setProfile(profile);
                        userRepository.save(user);
                    }

                    Account account = new Account();
                    account.setBalance(550);
                    account.setName(testName);
                    account.setUserId(user.getId());
                    account.setId(testName);
                    accountRepository.save(account);
                    return user;
                }).collect(Collectors.toList());
        return users;
    }

    public List<Transaction> getTestTransactions(String userId) {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(getNewTransaction(userId, -10, "BEIJING 8 DROTTN"));
        transactions.add(getNewTransaction(userId, -20, "HAIR SOLUTION"));
        transactions.add(getNewTransaction(userId, -30, "SALUPLATS HUSMAN"));
        transactions.add(getNewTransaction(userId, -40, "URBAN CAFE"));
        transactions.add(getNewTransaction(userId, -50, "ICA MAXI MATHORNAN S"));
        transactions.add(getNewTransaction(userId, -60, "CSN"));
        transactions.add(getNewTransaction(userId, -70, "B2 Bredband AB"));
        transactions.add(getNewTransaction(userId, -80, "TELE2 SVERIGE AB"));
        transactions.add(getNewTransaction(userId, -90, "TRANAN RESTAURAN"));
        transactions.add(getNewTransaction(userId, -100, "ICA DALASTAN"));
        transactions.add(getNewTransaction(userId, -80, "MAXI MAXI"));
        transactions.add(getNewTransaction(userId, -80, "COOP MAXI"));
        transactions.add(getNewTransaction(userId, -40, "URBAN CAFE"));
        transactions.add(getNewTransaction(userId, 400, "URBAN CAFE"));

        return transactions;
    }

    public Transaction getNewTransaction(String userId, double amount, String description) {
        return getNewTransaction(userId, amount, description, "0",
                "" + LocalDate.now().getDayOfMonth());
    }

    public Transaction getNewTransaction(String userId, double amount, String description, String monthsAgo,
                                            String day) {
        Transaction transaction = new Transaction();
        transaction.setOriginalDate(stringToDate(monthsAgo, day));
        transaction.setDate(stringToDate(monthsAgo, day));
        transaction.setAmount(amount);
        transaction.setOriginalAmount(amount);
        transaction.setDescription(description);
        transaction.setOriginalDescription(description);
        transaction.setAccountId(STATIC_ACCOUNT_ID);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setUserId(userId);
        transaction.setCredentialsId(userId);
        transaction.setType(TransactionTypes.DEFAULT);

        Category barCategory = categoryRepository.findByCode(categoryConfiguration.getBarsCode());

        transaction.setCategory(barCategory);

        return transaction;
    }

    public Date stringToDate(String monthsAgo, String day) {
        try {
            int dayOfMonth = Integer.parseInt(day);
            int minusMonths = Integer.parseInt(monthsAgo);
            LocalDate now = LocalDate.now();
            LocalDate twoMonthsAgo = now.minusMonths(minusMonths);

            if (dayOfMonth > 28 && twoMonthsAgo.getMonthOfYear() == 2) {
                dayOfMonth = 28;
            }

            twoMonthsAgo = twoMonthsAgo.withDayOfMonth(dayOfMonth);
            Date instant = twoMonthsAgo.toDate();
            return instant;
        } catch (NumberFormatException e) {
            throw new AssertionError("Couldn't string date to timestamp");
        }
    }

    public Credentials getCredentials(User user, String providerName) {
        return getCredentials(user, UUIDUtils.toTinkUUID(UUID.randomUUID()), providerName);
    }

    public Credentials getCredentials(User user, String credentialsId, String providername) {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setProviderName(providername);
        credentials.setUserId(user.getId());
        credentials.setUsername("test@tink.se");
        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setId(credentialsId);

        return credentials;
    }

    public UserData getUserData(User user, Credentials credentials,List<Account> accounts, List<Transaction> transactions) {
        UserData userData = new UserData();
        userData.setUser(user);
        userData.setTransactions(transactions);
        userData.setCredentials(Lists.newArrayList(credentials));
        userData.setAccounts(accounts);

        return userData;
    }

}

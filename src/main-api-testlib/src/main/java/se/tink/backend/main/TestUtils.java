package se.tink.backend.main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import se.tink.backend.common.utils.AccountBalanceUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.uuid.UUIDUtils;

public class TestUtils {

    public static User createUser(String username) {
        User user = new User();
        user.setProfile(createUserProfile("sv_SE", ResolutionTypes.MONTHLY_ADJUSTED, 25));
        user.setUsername(username);

        return user;
    }

    public static UserProfile createUserProfile(String locale, ResolutionTypes periodMode, int periodBreakDay) {
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale(locale);
        userProfile.setPeriodMode(periodMode);
        userProfile.setPeriodAdjustedDay(periodBreakDay);
        userProfile.setMarket(Market.Code.SE.name());

        return userProfile;
    }

    public static UserData createUserDate(User user, List<Account> accounts, List<Credentials> credentials,
            List<Transaction> transactions) {
        return createUserData(user, accounts, credentials, transactions, null);
    }

    public static UserData createUserData(User user, List<Account> accounts, List<Credentials> credentials,
            List<Transaction> transactions, List<AccountBalance> accountBalanceHistory) {
        UserData userData = new UserData();
        userData.setUser(user);
        userData.setAccounts(accounts);
        userData.setCredentials(credentials);
        userData.setTransactions(transactions);
        userData.setAccountBalanceHistory(accountBalanceHistory);

        return userData;
    }

    public static Account createAccount(String accId, String userId, String credentialsId, double balance,
            AccountTypes type) {
        Account account = new Account();
        account.setId(accId);
        account.setUserId(userId);
        account.setCredentialsId(credentialsId);
        account.setBalance(balance);
        account.setType(type);

        return account;
    }

    public static Account createAccount(int balance, boolean isExcluded) {
        String accId = UUIDUtils.toTinkUUID(UUID.randomUUID());
        String userId = UUIDUtils.toTinkUUID(UUID.randomUUID());

        Account account = createAccount(accId, userId, "credentialsId", balance, AccountTypes.DUMMY);
        account.setExcluded(isExcluded);

        return account;
    }

    public static Credentials createCregentials() {
        Credentials credentials = new Credentials();

        return credentials;
    }

    public static Category createCategory(String categoryCode) {
        Category category = new Category();
        category.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        category.setCode(categoryCode);

        if (categoryCode != null && !categoryCode.isEmpty()) {
            category.setType(CategoryTypes.valueOf(categoryCode.substring(0, categoryCode.indexOf(':')).toUpperCase()));
        }

        category.setSecondaryName(categoryCode);

        return category;
    }

    public static Transaction createTransaction(String description, double amount, Date date, String userId,
            Category category, String accountId) {
        return createTransaction(description, amount, TransactionTypes.DEFAULT, date, userId, category, accountId);
    }

    public static Transaction createTransaction(String description, double amount, TransactionTypes type, Date date,
            String userId, Category category, String accountId) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setUserId(userId);
        transaction.setDate(date);
        transaction.setAmount(amount);
        transaction.setType(type);

        if (category != null) {
            transaction.setCategory(category);
        }

        transaction.setAccountId(accountId);

        return transaction;
    }

    public static AccountBalance createAccountBalanceEntry(Account account, Date date, int balance) {
        return AccountBalanceUtils.createEntry(account.getUserId(), account.getId(), date, (double) balance,
                date.getTime());
    }

    /*
     * Convenience method to print statistics in a structured way.
     */
    public static void printStatistics(List<Statistic> statistics) {

        ImmutableList<Statistic> subset;

        ImmutableListMultimap<String, Statistic> statisticsByType = Multimaps.index(statistics,
                Statistic::getType);

        for (String type : statisticsByType.keySet()) {

            subset = statisticsByType.get(type);
            System.out.println(String.format("########## Type: %s (%d) ##########", type, subset.size()));

            ImmutableListMultimap<String, Statistic> statisticsByDescription = Multimaps.index(subset,
                    Statistic::getDescription);

            for (String description : statisticsByDescription.keySet()) {

                subset = statisticsByDescription.get(description);
                System.out.println(String.format("********** Description: %s (%d) **********", description,
                        subset.size()));

                ImmutableListMultimap<ResolutionTypes, Statistic> statisticsByResolution = Multimaps.index(subset,
                        Statistic::getResolution);

                for (ResolutionTypes resolution : statisticsByResolution.keySet()) {

                    subset = statisticsByResolution.get(resolution);
                    System.out.println(String.format("========== Resolution: %s (%d) ==========", resolution.name(),
                            subset.size()));

                    ImmutableListMultimap<String, Statistic> statisticsByPeriod = Multimaps.index(subset,
                            Statistic::getPeriod);

                    for (String period : statisticsByPeriod.keySet()) {

                        subset = statisticsByPeriod.get(period);
                        System.out
                                .println(String.format("---------- Period: %s (%d) ----------", period, subset.size()));

                        for (Statistic s : subset) {
                            System.out.println(String.format("Value: %s, Payload: %s", s.getValue(), s.getPayload()));
                        }
                    }
                }
            }
        }
    }

    public static Statistic createStatistic(String description, String period, double value, ResolutionTypes resolution,
            String type, String userId, String payload) {
        Statistic statistic = new Statistic();
        statistic.setDescription(description);
        statistic.setPeriod(period);
        statistic.setValue(value);
        statistic.setResolution(resolution);
        statistic.setType(type);
        statistic.setUserId(userId);
        statistic.setPayload(payload);
        return statistic;
    }
}

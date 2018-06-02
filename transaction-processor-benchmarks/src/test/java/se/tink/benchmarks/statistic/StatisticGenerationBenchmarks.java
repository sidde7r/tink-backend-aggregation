package se.tink.benchmarks.statistic;

import com.google.common.util.concurrent.MoreExecutors;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.statistics.StatisticsGenerator;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

@RunWith(MockitoJUnitRunner.class)
public class StatisticGenerationBenchmarks {
    private StatisticsGenerator generator;

    @Before
    public void setUp() {
        generator =  new StatisticsGenerator(
                MoreExecutors.newDirectExecutorService(),
                Collections.emptyList(),
                new SECategories(),
                new MetricRegistry(),
                Cluster.TINK);
    }

    @Test
    public void accountHistoryStatistics() {
        int monthlyBreakDay = 25;
        UserData userData = createUserData(monthlyBreakDay);

        MonthlyAdjustedPeriodizationFunction monthlyPeriodization= new MonthlyAdjustedPeriodizationFunction(
                monthlyBreakDay);
        Consumer<Integer> accountBalanceHistory = i -> generator.accountHistoryStatistics(
                userData,
                monthlyPeriodization);

        int warmUpIterations = 100;
        IntStream.range(0, warmUpIterations)
                .forEach(accountBalanceHistory::accept);

        long startMillis = System.currentTimeMillis();
        int iterations = 100;
        IntStream.range(0, iterations)
                .forEach(accountBalanceHistory::accept);
        long timeMillis = System.currentTimeMillis() - startMillis;
        System.out.println("Generated account balance history " + iterations + " times in " + timeMillis + "ms");
    }

    private UserData createUserData(int monthlyBreakDay) {
        UserData userData = new UserData();
        User user = new User();
        user.setId(UUIDUtils.generateUUID());
        UserProfile profile = new UserProfile();
        profile.setPeriodAdjustedDay(monthlyBreakDay);
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setLocale("en_SE");
        user.setProfile(profile);
        userData.setAccounts(generateAccounts());
        userData.setUser(user);
        userData.setAccountBalanceHistory(generateAccountBalanceHistory(userData.getAccounts()));
        return userData;
    }

    private List<Account> generateAccounts() {
        return Stream.of(AccountTypes.CHECKING, AccountTypes.CREDIT_CARD,
                AccountTypes.SAVINGS)
                .map(type -> {
                    Account account = new Account();
                    account.setId(UUIDUtils.generateUUID());
                    account.setType(type);
                    return account;
                })
                .collect(Collectors.toList());
    }

    private List<AccountBalance> generateAccountBalanceHistory(List<Account> accounts) {
        List<UUID> accountIds = accounts.stream()
                .map(Account::getId)
                .map(UUIDUtils::fromTinkUUID)
                .collect(Collectors.toList());
        int seed = 13;
        Random random = new Random(seed);
        LocalDateTime now = LocalDateTime.now();
        int budgetHistoryPoints = 500;
        double maxBalance = 8000;
        double balanceVariation = 3000;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return IntStream.range(0, budgetHistoryPoints)
                .mapToObj(i -> now.minusDays(i * 2 + random.nextInt(3)))
                .map(date -> {
                    AccountBalance balance = new AccountBalance();
                    balance.setAccountId(accountIds.get(random.nextInt(accountIds.size())));
                    balance.setBalance(Math.round((maxBalance - random.nextDouble() * balanceVariation) * 100) / 100.0);
                    balance.setDate(Integer.valueOf(date.format(dateFormatter)));
                    balance.setInserted(date.toEpochSecond(ZoneOffset.UTC));
                    return balance;
                })
                .collect(Collectors.toList());
    }

}

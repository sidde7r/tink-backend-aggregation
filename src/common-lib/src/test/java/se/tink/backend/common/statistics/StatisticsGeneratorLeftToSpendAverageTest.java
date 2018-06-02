package se.tink.backend.common.statistics;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Category;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.metrics.MetricRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_Monthly;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.stubBuilder;

public class StatisticsGeneratorLeftToSpendAverageTest extends StatisticGeneratorUnitTest {
    private StatisticsGenerator statisticsGenerator;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    /**
     * Based on StatisticsLeftToSpendAverageFucntionTest to assert the same values.
     */

    @Before
    public void setUp() throws ParseException {
        List<Category> categories = generateCategories();
        statisticsGenerator = new StatisticsGenerator(categories, new SECategories(), new MetricRegistry(), Cluster.TINK);
    }

    @Test
    public void testAverageBasedOnTwoMonths() throws ParseException {
        int periodAdjustedDay = 1;
        User user = createUser("test", periodAdjustedDay);
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(TestUtils.createTransaction("Expense 1", 15d, formatter.parse("2015/12/01"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 2", 15d, formatter.parse("2015/12/24"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 3", 5d, formatter.parse("2016/01/01"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 4", 15d, formatter.parse("2016/01/24"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 5", 45d, formatter.parse("2016/02/01"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 6", 9d, formatter.parse("2016/02/24"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 7", 40d, formatter.parse("2016/03/04"), user.getId(), incomeCategories.get(0), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrap(statisticsGenerator.transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(periodAdjustedDay)));
        userData.addStatistics(statistics);

        List<Statistic> averages = statisticsGenerator
                .generateStatisticsBasedStatistics(userData, new MonthlyAdjustedPeriodizationFunction(periodAdjustedDay), 2);
        averages.sort(Comparator.comparing(Statistic::getDescription));


        assertThat(averages).isNotEmpty();
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2016-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2016-02");
        ImmutableList<Statistic> march = getSortedPeriod(averages, "2016-03");

        assertThat(averages).hasSize(31 + 31 + 29 + 31);
        assertThat(january).hasSize(31);
        assertThat(february).hasSize(29);
        assertThat(march).hasSize(31);
        assertThat(january).isNotEqualTo(march);

        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Monthly("2016-03", "2016-03-22", 25, user.getId(), false))
                .add(createLTSA_Monthly("2016-03", "2016-03-01", 25, user.getId(), false))
                .add(createLTSA_Monthly("2016-03", "2016-03-30", 37.0, user.getId(), false))
                .add(createLTSA_Monthly("2016-02", "2016-02-01", 10.0, user.getId(), false))
                .add(createLTSA_Monthly("2016-02", "2016-02-24", 25.0, user.getId(), false))
                .add(createLTSA_Monthly("2016-02", "2016-02-28", 25.0, user.getId(), false))
                .add(createLTSA_Monthly("2016-01", "2016-01-01", 15.0, user.getId(), false))
                .add(createLTSA_Monthly("2016-01", "2016-01-25", 30.0, user.getId(), false))
                .add(createLTSA_Monthly("2016-01", "2016-01-28", 30.0, user.getId(), false))
                .add(createLTSA_Monthly("2015-12", "2015-12-01", 0.0, user.getId(), false))
                .add(createLTSA_Monthly("2015-12", "2015-12-24", 0.0, user.getId(), false))
                .add(createLTSA_Monthly("2015-12", "2015-12-28", 0.0, user.getId(), false))
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    @Test
    public void testAverageBasedOnTwoMonthsMonthlyAdjusted() throws ParseException {
        int periodAdjustedDay = 25;
        User user = createUser("test", periodAdjustedDay);
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(TestUtils.createTransaction("Expense 1", 150d, formatter.parse("2015/11/15"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 2", 15d, formatter.parse("2015/12/01"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 3", 15d, formatter.parse("2015/12/20"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 4", 5d, formatter.parse("2016/01/1"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 4", 15d, formatter.parse("2016/01/20"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 5", 45d, formatter.parse("2016/02/01"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 6", 9d, formatter.parse("2016/02/20"), user.getId(), incomeCategories.get(0), account.getId()));
        transactions.add(TestUtils.createTransaction("Expense 7", 40d, formatter.parse("2016/03/04"), user.getId(), incomeCategories.get(0), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrap(statisticsGenerator.transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(periodAdjustedDay)));
        userData.addStatistics(statistics);

        TestUtils.printStatistics(statistics);

        List<Statistic> averages = statisticsGenerator
                .generateStatisticsBasedStatistics(userData, new MonthlyAdjustedPeriodizationFunction(periodAdjustedDay), 2);
        averages.sort(Comparator.comparing(Statistic::getDescription));

        assertThat(averages).isNotEmpty();
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2016-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2016-02");
        ImmutableList<Statistic> march = getSortedPeriod(averages, "2016-03");

        assertThat(averages).hasSize(30 + 31 + 33 + 31 + 28);
        assertThat(january).hasSize(33);
        assertThat(february).hasSize(31);
        assertThat(march).hasSize(28);
        assertThat(january).isNotEqualTo(march);

        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Monthly("2016-03", "2016-02-25", 0, user.getId(), true))
                .add(createLTSA_Monthly("2016-03", "2016-03-05", 25, user.getId(), true))
                .add(createLTSA_Monthly("2016-03", "2016-03-23", 37.0, user.getId(), true))
                .add(createLTSA_Monthly("2016-02", "2016-02-03", 10.0, user.getId(), true))
                .add(createLTSA_Monthly("2016-02", "2016-02-22", 25.0, user.getId(), true))
                .add(createLTSA_Monthly("2016-02", "2016-02-24", 25.0, user.getId(), true))
                .add(createLTSA_Monthly("2016-01", "2015-12-23", 0.0, user.getId(), true))
                .add(createLTSA_Monthly("2016-01", "2015-12-31", 15.0, user.getId(), true))
                .add(createLTSA_Monthly("2016-01", "2016-01-24", 30.0, user.getId(), true))
                .add(createLTSA_Monthly("2015-12", "2015-11-25", 0.0, user.getId(), true))
                .add(createLTSA_Monthly("2015-12", "2015-12-02", 0.0, user.getId(), true))
                .add(createLTSA_Monthly("2015-12", "2015-12-22", 0.0, user.getId(), true))
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    private UserData createUserDate(User user, List<Transaction> transactions, Account account) {
        return TestUtils.createUserData(user, Lists.newArrayList(account),
                Lists.newArrayList(TestUtils.createCregentials()), transactions,
                Lists.<AccountBalance>newArrayList());
    }

    private ImmutableList<Statistic> getSortedPeriod(Iterable<Statistic> averages, final String period) {
        return FluentIterable.from(averages)
                .filter(statistic -> Objects.equal(statistic.getPeriod(), period))
                .toSortedList((o1, o2) -> o1.getDescription().compareTo(o2.getDescription()));
    }

    private static User createUser(String username, int periodBreakDay) {
        User user = TestUtils.createUser(username);
        if (periodBreakDay == 1) {
            user.getProfile().setPeriodAdjustedDay(periodBreakDay);
            user.getProfile().setPeriodMode(ResolutionTypes.MONTHLY);
        } else {
            user.getProfile().setPeriodAdjustedDay(periodBreakDay);
            user.getProfile().setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        }

        return user;
    }

}

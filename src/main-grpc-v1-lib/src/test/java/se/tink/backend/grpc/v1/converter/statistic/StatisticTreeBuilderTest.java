package se.tink.backend.grpc.v1.converter.statistic;

import java.text.ParseException;
import java.util.Date;
import org.junit.Test;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.grpc.v1.utils.StatisticTreeBuilder;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.libraries.date.ResolutionTypes.DAILY;
import static se.tink.libraries.date.ResolutionTypes.MONTHLY;
import static se.tink.libraries.date.ResolutionTypes.MONTHLY_ADJUSTED;
import static se.tink.libraries.date.ResolutionTypes.WEEKLY;
import static se.tink.libraries.date.ResolutionTypes.YEARLY;

public class StatisticTreeBuilderTest {

    public void assertPeriod(Period period, ResolutionTypes resolution, Date... datesInside) {
        assertThat(period.getResolution()).isEqualTo(resolution);
        for (Date date : datesInside) {
            assertThat(period.isDateWithinInclusive(date)).isTrue();
        }
    }

    public void assertNode(StatisticNode node, Statistic statistic, Date... datesInside) {
        assertThat(node).isNotNull();
        assertThat(node.getDescription()).isEqualTo(statistic.getDescription());
        assertThat(node.getAmount()).isEqualTo(statistic.getValue());
        assertPeriod(node.getPeriod(), statistic.getResolution(), datesInside);
    }

    public void assertAmounts(StatisticNode node, double amount) {
        assertThat(node.getAmount()).isEqualTo(amount);
    }

    private Statistic createStatistic(String description, String period, double value, ResolutionTypes resolution) {
        return TestUtils.createStatistic(description, period, value, resolution, "type", "userId", "payload");
    }

    private UserProfile defaultMonthlyAdjUserProfile() {
        return TestUtils.createUserProfile("sv_SE", ResolutionTypes.MONTHLY_ADJUSTED, 25);
    }

    @Test
    public void buildTreeForOneDailyStatistic() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(
                TestUtils.createUserProfile("sv_SE", ResolutionTypes.MONTHLY, 25));

        Statistic dailyStatistic = createStatistic("Daily expenses", "2017-03-03", 100d, DAILY);
        Date statisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());
        builder.addNode(dailyStatistic);

        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(1);
        StatisticNode monthlyNode = root.getChildren().get("2017-03");
        assertThat(monthlyNode).isNotNull();
        assertPeriod(monthlyNode.getPeriod(), MONTHLY, statisticDate);

        // assert daily nodes
        assertThat(monthlyNode.getChildren()).hasSize(1);
        assertNode(monthlyNode.getChildren().get("2017-03-03"), dailyStatistic, statisticDate);
    }

    @Test
    public void buildTreeForOneDailyStatisticWithTrickyMonthlyAdjusted() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(
                TestUtils.createUserProfile("sv_SE", ResolutionTypes.MONTHLY_ADJUSTED, 1));

        Statistic dailyStatistic = createStatistic("Daily expenses", "2017-03-03", 100d, DAILY);
        Date statisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        builder.addNode(dailyStatistic);
        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(1);
        StatisticNode monthlyNode = root.getChildren().get("2017-04");
        assertThat(monthlyNode).isNotNull();
        assertPeriod(monthlyNode.getPeriod(), MONTHLY_ADJUSTED, statisticDate);

        // assert daily nodes
        assertThat(monthlyNode.getChildren()).hasSize(1);
        assertNode(monthlyNode.getChildren().get("2017-03-03"), dailyStatistic, statisticDate);
    }

    @Test
    public void buildTreeForTwoDailyStatisticWithSameMonth() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic dailyStatistic1 = createStatistic("Daily expenses", "2017-03-03", 100d, DAILY);
        Statistic dailyStatistic2 = createStatistic("Daily expenses", "2017-03-04", 200d, DAILY);

        Date statistic1Date = DateUtils.parseDate(dailyStatistic1.getPeriod());
        Date statistic2Date = DateUtils.parseDate(dailyStatistic2.getPeriod());

        builder.addNode(dailyStatistic1);
        builder.addNode(dailyStatistic2);
        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(1);
        StatisticNode monthlyNode = root.getChildren().get("2017-03");
        assertThat(monthlyNode).isNotNull();
        assertPeriod(monthlyNode.getPeriod(), MONTHLY_ADJUSTED, statistic1Date, statistic2Date);

        // assert daily nodes
        assertThat(monthlyNode.getChildren()).hasSize(2);
        assertNode(monthlyNode.getChildren().get("2017-03-03"), dailyStatistic1, statistic1Date);
        assertNode(monthlyNode.getChildren().get("2017-03-04"), dailyStatistic2, statistic2Date);
    }

    @Test
    public void buildTreeForOneDailyStatisticAndThenForSameMonth() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic dailyStatistic = createStatistic("Daily expenses", "2017-03-03", 100d, DAILY);
        Date dailyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        Statistic monthlyStatistic = createStatistic("Monthly expenses", "2017-03", 3100d, MONTHLY_ADJUSTED);
        Date monthlyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        builder.addNode(dailyStatistic);
        builder.addNode(monthlyStatistic);
        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(1);
        StatisticNode monthlyNode = root.getChildren().get("2017-03");
        assertAmounts(monthlyNode, dailyStatistic.getValue());

        // assert daily nodes
        assertThat(monthlyNode.getChildren()).hasSize(1);
        assertNode(monthlyNode.getChildren().get("2017-03-03"), dailyStatistic, dailyStatisticDate);
    }

    @Test
    public void buildTreeForOneMonthlyStatisticAndThenForSameDailyPeriod() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic dailyStatistic = createStatistic("Daily expenses", "2017-03-03", 100d, DAILY);
        Date dailyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        Statistic monthlyStatistic = createStatistic("Monthly expenses", "2017-03", 3100d, MONTHLY_ADJUSTED);
        Date monthlyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        builder.addNode(monthlyStatistic);
        builder.addNode(dailyStatistic);
        StatisticNode root = builder.build();

        // assert year nodes
        assertThat(root.getChildren()).hasSize(1);
        StatisticNode monthlyNode = root.getChildren().get("2017-03");
        assertAmounts(monthlyNode, dailyStatistic.getValue());

        // assert daily nodes
        assertThat(monthlyNode.getChildren()).hasSize(1);
        assertNode(monthlyNode.getChildren().get("2017-03-03"), dailyStatistic, dailyStatisticDate);
    }

    @Test
    public void buildTreeForTwoDailyStatisticWithDifferentMonths() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic dailyStatistic1 = createStatistic("Daily expenses", "2017-02-03", 100d, DAILY);
        Statistic dailyStatistic2 = createStatistic("Daily expenses", "2017-03-04", 200d, DAILY);

        Date statistic1Date = DateUtils.parseDate(dailyStatistic1.getPeriod());
        Date statistic2Date = DateUtils.parseDate(dailyStatistic2.getPeriod());

        builder.addNode(dailyStatistic1);
        builder.addNode(dailyStatistic2);
        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(2);
        StatisticNode monthly02 = root.getChildren().get("2017-02");
        StatisticNode monthly03 = root.getChildren().get("2017-03");
        assertThat(monthly02).isNotNull();
        assertThat(monthly03).isNotNull();
        assertPeriod(monthly02.getPeriod(), MONTHLY_ADJUSTED, statistic1Date);
        assertPeriod(monthly03.getPeriod(), MONTHLY_ADJUSTED, statistic2Date);

        // assert daily nodes
        assertThat(monthly02.getChildren()).hasSize(1);
        assertNode(monthly02.getChildren().get("2017-02-03"), dailyStatistic1, statistic1Date);

        assertThat(monthly03.getChildren()).hasSize(1);
        assertNode(monthly03.getChildren().get("2017-03-04"), dailyStatistic2, statistic2Date);
    }

    @Test
    public void buildTreeForDailyAndMonthlyStatisticsWithDifferentYears() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic dailyStatistic = createStatistic("Daily expenses", "2016-02-03", 100d, DAILY);
        Statistic monthlyStatistic = createStatistic("Monthly expenses", "2017-03", 200d, MONTHLY_ADJUSTED);

        Date dailyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());
        Date monthlyStatisticDate = DateUtils.parseDate(monthlyStatistic.getPeriod());

        builder.addNode(dailyStatistic);
        builder.addNode(monthlyStatistic);
        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(2);
        StatisticNode monthlyNode1 = root.getChildren().get("2016-02");
        assertThat(monthlyNode1).isNotNull();
        assertPeriod(monthlyNode1.getPeriod(), MONTHLY_ADJUSTED, dailyStatisticDate);
        StatisticNode monthlyNode2 = root.getChildren().get("2017-03");
        assertThat(monthlyNode2).isNotNull();
        assertPeriod(monthlyNode2.getPeriod(), MONTHLY_ADJUSTED, monthlyStatisticDate);

        // assert daily nodes
        assertThat(monthlyNode1.getChildren()).hasSize(1);
        assertNode(monthlyNode1.getChildren().get("2016-02-03"), dailyStatistic, dailyStatisticDate);

        assertThat(monthlyNode2.getChildren()).isEmpty();
    }

    @Test
    public void buildTreeForWeeklyAndMonthlyStatisticsForSameYear() throws ParseException {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic weeklyStatistic = createStatistic("Weekly expenses", "2017:11", 100d,
                WEEKLY); // 2017-03-13 - 2017-03-19
        Statistic monthlyStatistic = createStatistic("Monthly expenses", "2017-03", 200d, MONTHLY_ADJUSTED);

        Date weeklyStatisticDate = ThreadSafeDateFormat.FORMATTER_WEEKLY.parse(weeklyStatistic.getPeriod());
        Date monthlyStatisticDate = DateUtils.parseDate(monthlyStatistic.getPeriod());

        builder.addNode(weeklyStatistic);
        builder.addNode(monthlyStatistic);
        StatisticNode root = builder.build();

        // assert monthly & weekly nodes
        assertThat(root.getChildren()).hasSize(2);
        StatisticNode monthlyNode = root.getChildren().get("2017-03");
        StatisticNode weeklyNode = root.getChildren().get("2017:11");

        assertNode(weeklyNode, weeklyStatistic, weeklyStatisticDate);
        assertNode(monthlyNode, monthlyStatistic, monthlyStatisticDate);

        // assert daily nodes
        for (StatisticNode yearlyNodeChild : root.getChildren().values()) {
            assertThat(yearlyNodeChild.getChildren()).isEmpty();
        }
    }

    @Test
    public void buildTreeForWeeklyMonthlyAndDailyStatisticsForSamePeriods() throws ParseException {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        Statistic dailyStatistic = createStatistic("Daily expenses", "2017-03-15", 100d, DAILY);
        Statistic weeklyStatistic = createStatistic("Weekly expenses", "2017:11", 1200d,
                WEEKLY); // 2017-03-13 - 2017-03-19
        Statistic monthlyStatistic = createStatistic("Monthly expenses", "2017-03", 2300d, MONTHLY_ADJUSTED);

        Date weeklyStatisticDate = ThreadSafeDateFormat.FORMATTER_WEEKLY.parse(weeklyStatistic.getPeriod());
        Date monthlyStatisticDate = DateUtils.parseDate(monthlyStatistic.getPeriod());
        Date dailyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        builder.addNode(weeklyStatistic);
        builder.addNode(monthlyStatistic);
        builder.addNode(dailyStatistic);
        StatisticNode root = builder.build();

        // assert monthly & weekly nodes
        assertThat(root.getChildren()).hasSize(2);
        StatisticNode monthlyNode = root.getChildren().get("2017-03");
        StatisticNode weeklyNode = root.getChildren().get("2017:11");

        assertNode(weeklyNode, weeklyStatistic, weeklyStatisticDate, dailyStatisticDate);
        assertAmounts(monthlyNode, dailyStatistic.getValue());

        // assert daily nodes
        assertThat(monthlyNode.getChildren()).hasSize(1);
        assertNode(monthlyNode.getChildren().get("2017-03-15"), dailyStatistic, dailyStatisticDate);

        assertThat(weeklyNode.getChildren()).isEmpty();
    }

    @Test
    public void buildTreeForWeeklyStatisticsForLastYearButForCurrentYearPeriodMode() throws ParseException {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(
                TestUtils.createUserProfile("sv_SE", ResolutionTypes.MONTHLY_ADJUSTED, 1));

        Statistic weeklyStatistic = createStatistic("Weekly expenses", "2016:52", 12300d, WEEKLY); // 2016 year
        Statistic dailyStatistic = createStatistic("Daily expenses", "2016-12-15", 200d, DAILY); // 2017 year

        Date weeklyStatisticDate = ThreadSafeDateFormat.FORMATTER_WEEKLY.parse(weeklyStatistic.getPeriod());
        Date dailyStatisticDate = DateUtils.parseDate(dailyStatistic.getPeriod());

        builder.addNode(weeklyStatistic);
        builder.addNode(dailyStatistic);
        StatisticNode root = builder.build();

        // assert monthly & weekly nodes
        assertThat(root.getChildren()).hasSize(2);
        StatisticNode weeklyNode = root.getChildren().get("2016:52");
        assertPeriod(weeklyNode.getPeriod(), WEEKLY, weeklyStatisticDate);
        assertThat(weeklyNode.getChildren()).isEmpty();
    }

    @Test
    public void checkMonthlyAmountIsLastDayAmount() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());
        // May, 2 daily statistics. Sum: 1_000
        builder.addNode(createStatistic("Daily expenses", "2017-05-04", 400d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-05-05", 600d, DAILY));

        // June, 2 daily statistic and 1 monthlys. Sum: 1_000
        builder.addNode(createStatistic("Daily expenses", "2017-06-04", 100d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-06-05", 400d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-06-06", 500d, DAILY));
        builder.addNode(createStatistic("Monthly expenses", "2017-06", 1_000d, MONTHLY_ADJUSTED));

        // July, 1 monthly and 2 daily statistics. Sum: 2_000
        builder.addNode(createStatistic("Monthly expenses", "2017-07", 1_000d, MONTHLY_ADJUSTED));
        builder.addNode(createStatistic("Daily expenses", "2017-07-04", 700d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-07-05", 300d, DAILY));

        // September, 1 monthly statistic. Sum: 5_000
        builder.addNode(createStatistic("Monthly expenses", "2017-09", 5_000d, MONTHLY_ADJUSTED));

        // Weekly statistic should not be aggregated. Sum: 0
        builder.addNode(createStatistic("Monthly expenses", "2017:09", 25_000d, WEEKLY));

        StatisticNode root = builder.build();

        // assert monthly & weekly nodes
        assertThat(root.getChildren()).hasSize(5); // 4 months and 1 week
        assertThat(root.getChildren().keySet()).containsOnly("2017-05", "2017-06", "2017-07", "2017-09", "2017:09");

        // assert week values
        assertAmounts(root.getChildren().get("2017:09"), 25_000);

        // assert months values
        assertAmounts(root.getChildren().get("2017-05"), 600);
        assertAmounts(root.getChildren().get("2017-06"), 500);
        assertAmounts(root.getChildren().get("2017-07"), 300);
        assertAmounts(root.getChildren().get("2017-09"), 5_000);
    }

    @Test
    public void checkUpdatedSum() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());

        builder.addNode(createStatistic("Daily expenses", "2017-05-05", 400d, DAILY));
        builder.addNode(createStatistic("Updated daily expenses", "2017-05-05", 1_000d, DAILY));

        StatisticNode root = builder.build();

        // assert monthly nodes
        assertThat(root.getChildren()).hasSize(1);
        StatisticNode month = root.getChildren().get("2017-05");
        assertThat(month).isNotNull();
        assertAmounts(month, 1_000);

        // assert daily nodes
        assertThat(month.getChildren()).hasSize(1);
        StatisticNode day = month.getChildren().get("2017-05-05");
        assertThat(day).isNotNull();
        assertAmounts(day, 1_000);
    }

    @Test
    public void checkRootPeriod() {
        StatisticTreeBuilder builder = new StatisticTreeBuilder(defaultMonthlyAdjUserProfile());
        builder.addNode(createStatistic("Daily expenses", "2017-05-04", 400d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-05-05", 600d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-06-04", 100d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-06-05", 100d, DAILY));
        builder.addNode(createStatistic("Monthly expenses", "2017-06", 1_000d, MONTHLY_ADJUSTED));
        builder.addNode(createStatistic("Monthly expenses", "2017-07", 2_000d, MONTHLY_ADJUSTED));
        builder.addNode(createStatistic("Daily expenses", "2017-07-04", 200d, DAILY));
        builder.addNode(createStatistic("Daily expenses", "2017-07-05", 200d, DAILY));
        builder.addNode(createStatistic("Monthly expenses", "2017-09", 5_000d, MONTHLY_ADJUSTED));
        builder.addNode(createStatistic("Monthly expenses", "2017:30", 25_000d, WEEKLY));

        StatisticNode root = builder.build();
        assertThat(root.getPeriod()).isNotNull();
        assertThat(root.getPeriod().getStartDate())
                .hasSameTimeAs(DateUtils.setInclusiveStartTime(DateUtils.parseDate("2017-05-04")));
        assertThat(root.getPeriod().getEndDate())
                .hasSameTimeAs(DateUtils.setInclusiveEndTime(DateUtils.parseDate("2017-09-24")));
    }
}

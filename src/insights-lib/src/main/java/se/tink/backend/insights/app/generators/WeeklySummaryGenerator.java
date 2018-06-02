package se.tink.backend.insights.app.generators;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateWeeklySummaryInsightCommand;
import se.tink.backend.insights.app.queryservices.CategoryQueryService;
import se.tink.backend.insights.app.queryservices.StatisticsQueryService;
import se.tink.backend.insights.core.domain.contents.WeeklySummaryInsightCategoryData;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.core.valueobjects.WeeklyTransactions;
import se.tink.backend.insights.transactions.TransactionQueryService;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class WeeklySummaryGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(WeeklySummaryGenerator.class);

    private CommandGateway commandGateway;
    private CategoryQueryService categoryQueryService;
    private TransactionQueryService transactionQueryService;
    private UserQueryService userQueryService;
    private StatisticsQueryService statisticsQueryService;

    @Inject
    public WeeklySummaryGenerator(CommandGateway commandGateway,
            CategoryQueryService categoryQueryService,
            TransactionQueryService transactionQueryService,
            UserQueryService userQueryService,
            StatisticsQueryService statisticsQueryService) {
        this.commandGateway = commandGateway;
        this.categoryQueryService = categoryQueryService;
        this.transactionQueryService = transactionQueryService;
        this.userQueryService = userQueryService;
        this.statisticsQueryService = statisticsQueryService;
    }

    @Override
    public void generateIfShould(UserId userId) {

        //TODO: include daily expenses and average daily expenses over time


        // TODO: move all the statistics logic in the statistics service
        User user = userQueryService.findById(userId);
        String uncategorizedCategoryId = categoryQueryService.getUnknownCategoryExpensesId(user.getLocale());

        List<Statistic> statistics = statisticsQueryService.getUserStatistics(userId);

        //TODO: move the following to query service
        ImmutableListMultimap<ResolutionTypes, Statistic> statisticsByResolution = Multimaps.index(statistics,
                Statistic::getResolution);

        ImmutableListMultimap<String, Statistic> statisticsByTypeWeekly = Multimaps.index(
                statisticsByResolution.get(ResolutionTypes.WEEKLY), Statistic::getType);

        ListMultimap<String, Statistic> interestingExpensesByPeriodStatistics = Multimaps.index(
                statisticsByTypeWeekly.get(Statistic.Types.EXPENSES_BY_CATEGORY), Statistic::getPeriod);

        ListMultimap<String, Statistic> interestingExpensesByPeriodByCountStatistics = Multimaps.index(
                statisticsByTypeWeekly.get(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY),
                Statistic::getPeriod);

        // Construct the weekly boundary calendars.

        Calendar weekStartCalendar = DateUtils.getCalendar(); // DEFAULT_LOCALE for now
        // Start of current week
        weekStartCalendar = DateUtils.getFirstDateOfWeek(weekStartCalendar);
        // Start of previous week
        weekStartCalendar.add(Calendar.DAY_OF_YEAR, -7);
        DateUtils.setInclusiveStartTime(weekStartCalendar);

        Calendar weekEndCalendar = (Calendar) weekStartCalendar.clone();
        weekEndCalendar.add(Calendar.DAY_OF_YEAR, 6);
        DateUtils.setInclusiveEndTime(weekEndCalendar);

        final int weekOfYear = weekStartCalendar.get(Calendar.WEEK_OF_YEAR);
        final Date weekEndDate = weekEndCalendar.getTime();
        final Date weekStartDate = weekStartCalendar.getTime();
        final String weekPeriod;
        final String yearOfWeekPeriod;

        // Highest number weeks always has the year of the date at the start of the week. The opposite for first
        // weeks.
        if (weekOfYear <= 52) {
            yearOfWeekPeriod = ThreadSafeDateFormat.FORMATTER_YEARLY.format(weekEndCalendar.getTime());
        } else {
            yearOfWeekPeriod = ThreadSafeDateFormat.FORMATTER_YEARLY.format(weekStartCalendar.getTime());
        }

        weekPeriod = String.format("%s:%s", yearOfWeekPeriod,
                Strings.padStart(Integer.toString(weekOfYear), 2, '0'));

        List<Statistic> weekInterestingExpensesByCategory = interestingExpensesByPeriodStatistics.get(weekPeriod);
        List<Statistic> weekInterestingExpensesByCategoryCount = interestingExpensesByPeriodByCountStatistics
                .get(weekPeriod);

        // The (k=3) largest expenses by categories
        Iterable<Statistic> largestCategoryExpenses = statisticsQueryService.STATISTICS_VALUE_ORDERING.leastOf(
                weekInterestingExpensesByCategory.stream()
                        .filter(s -> (!Objects.equal(s.getDescription(), uncategorizedCategoryId)))
                        .collect(Collectors.toList()), 3);

        WeeklyTransactions weeklyExpenseTransactions = transactionQueryService.findLastWeeksExpenseTransactions(userId);

        List<WeeklySummaryInsightCategoryData> weeklySummaryInsightCategoryDataList = Lists.newArrayList();

        for (Statistic s : largestCategoryExpenses) {
            final WeeklySummaryInsightCategoryData categorySummaryData = new WeeklySummaryInsightCategoryData(
                    categoryQueryService.getCategoryDisplayName(s.getDescription()), s.getValue(),
                    (int) weekInterestingExpensesByCategoryCount.stream()
                            .filter(s1 -> Objects.equal(s1.getDescription(), s.getDescription())).findFirst().get().getValue());

            weeklySummaryInsightCategoryDataList.add(categorySummaryData);
        }

        // TODO: add some default largest transaction
        if (!weeklyExpenseTransactions.getLargestTransaction().isPresent()) {
            log.info(userId, "No insight generated. Reason: No largest transaction found");
            return;
        }

        CreateWeeklySummaryInsightCommand command = new CreateWeeklySummaryInsightCommand(
                userId,
                Amount.of(weeklyExpenseTransactions.getTotalAmountInTransactions()),
                weeklyExpenseTransactions.getLargestTransaction().get(),
                weeklySummaryInsightCategoryDataList,
                weeklyExpenseTransactions.getTransactionsCount(),
                weeklyExpenseTransactions.getWeek()
        );
        commandGateway.on(command);
    }

}

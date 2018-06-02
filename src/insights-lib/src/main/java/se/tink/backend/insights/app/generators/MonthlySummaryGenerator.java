package se.tink.backend.insights.app.generators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateMonthlySummaryInsightCommand;
import se.tink.backend.insights.app.queryservices.StatisticsQueryService;
import se.tink.backend.insights.core.domain.contents.MonthlySummaryInsightCategoryData;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.MonthlyTransactions;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.transactions.TransactionQueryService;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;

public class MonthlySummaryGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(MonthlySummaryGenerator.class);

    private CommandGateway commandGateway;
    private TransactionQueryService transactionQueryService;
    private UserQueryService userQueryService;
    private StatisticsQueryService statisticsQueryService;
    // TODO: refactor below
    private UserStateRepository userStateRepository;

    @Inject
    public MonthlySummaryGenerator(CommandGateway commandGateway,
            TransactionQueryService transactionQueryService,
            UserQueryService userQueryService,
            StatisticsQueryService statisticsQueryService,
            UserStateRepository userStateRepository) {
        this.commandGateway = commandGateway;
        this.transactionQueryService = transactionQueryService;
        this.userQueryService = userQueryService;
        this.statisticsQueryService = statisticsQueryService;
        this.userStateRepository = userStateRepository;
    }

    @Override
    // Trigger should only depend on period/salary date
    public void generateIfShould(UserId userId) {

        // Fixme: Remove User
        User user = userQueryService.findById(userId);
        List<Statistic> statistics = statisticsQueryService.getUserStatistics(userId);

        final ResolutionTypes periodMode = user.getProfile().getPeriodMode();
        ImmutableList<Statistic> statisticsByPeriodMode = statisticsQueryService
                .filterByResolution(statistics, periodMode);
        ImmutableList<Statistic> incomeExpensesStatistics = statisticsQueryService
                .filterByStatisticType(statisticsByPeriodMode, Statistic.Types.INCOME_AND_EXPENSES);
        ImmutableList<Statistic> expensesStatistics = statisticsQueryService
                .filterByCategoryName(incomeExpensesStatistics, CategoryTypes.EXPENSES.name());

        ImmutableMap<String, Statistic> expensesByPeriod = Maps.uniqueIndex(expensesStatistics, Statistic::getPeriod);

        UserState userState = userStateRepository.findOneByUserId(user.getId());
        final ImmutableList<Period> sortedPeriods = PERIODS_ORDERING.immutableSortedCopy(userState.getPeriods())
                .reverse();

        Optional<Period> possiblePeriod = latestPeriod(sortedPeriods);
        if (!possiblePeriod.isPresent()) {
            return;
        }
        Period period = possiblePeriod.get();

        Statistic periodExpensesStatistic = expensesByPeriod.get(period.getName());

        double periodExpenses = (periodExpensesStatistic != null) ? periodExpensesStatistic.getValue() : 0;

        // Don't generate activity if we haven't any expenses in this period. Note: why? We can still have zero expenses in the summary
        if (periodExpenses == 0) {
            log.info(userId, "No insight generated. Reason: No expenses");
            //            return;
        }

        double periodExpensesAvg = getPeriodExpensesAvg(expensesByPeriod, sortedPeriods, 0);

        List<MonthlySummaryInsightCategoryData> largestCategories = statisticsQueryService
                .largestCategoryExpensesByPeriod(statisticsByPeriodMode, period);

        // Get the largest expense transaction from latest period

        MonthlyTransactions monthlyExpenses = transactionQueryService.findPreviousMonthsExpenseTransactions(userId);

        // TODO: Set some default largest expense - but if there exist any expenses one of them must be the largest..
        if (!monthlyExpenses.getLargestTransaction().isPresent()) {
            log.info(userId, "No insight generated. Reason: No largest expense found");
            return;
        }

        CreateMonthlySummaryInsightCommand command = new CreateMonthlySummaryInsightCommand(
                userId,
                period,
                Amount.of(periodExpenses),
                periodExpensesAvg,
                monthlyExpenses.getLargestTransaction().get(),
                largestCategories

        );
        commandGateway.on(command);
    }

    private Optional<Period> latestPeriod(List<Period> sortedPeriods) {
        // Get statistic by period
        Date today = new Date();

        // Get latest period
        Period period = sortedPeriods.get(0);
        if (!period.isClean()) {
            log.info("Period clean.");
            return Optional.empty();
        }

        Date periodEndDate = period.getEndDate();

        // If period end date is in the future, the month isn't over yet.
        if (periodEndDate == null || periodEndDate.after(today)) {
            if (sortedPeriods.size() > 1) {
                log.info("no Insight for period '" + period.toString() + "'. Getting one period before");
                return Optional.of(sortedPeriods.get(1));
            }
            log.info("no Insight for period '" + period.toString() + "'. No period before this period");
            return Optional.empty();
        }
        return Optional.of(period);
    }

    private double getPeriodExpensesAvg(ImmutableMap<String, Statistic> expensesByPeriod, List<Period> periods,
            int periodIndex) {
        final int PERIOD_COUNT_FOR_AVERAGE_CALCULATION = 6;

        double sum = 0;
        for (int i = periodIndex;
             i < Math.min(expensesByPeriod.size(), periodIndex + PERIOD_COUNT_FOR_AVERAGE_CALCULATION); i++) {
            Period period = periods.get(i);
            Statistic statistic = expensesByPeriod.get(period
                    .getName());
            sum += (statistic != null) ? statistic.getValue() : 0;
        }
        return sum / Math.min(expensesByPeriod.size() - periodIndex, PERIOD_COUNT_FOR_AVERAGE_CALCULATION);
    }

    private static final Ordering<Period> PERIODS_ORDERING = new Ordering<Period>() {
        @Override
        public int compare(Period p1, Period p2) {
            return p1.getName().compareTo(p2.getName());
        }
    };
}

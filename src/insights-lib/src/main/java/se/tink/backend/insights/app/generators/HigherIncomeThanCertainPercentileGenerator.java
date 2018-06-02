package se.tink.backend.insights.app.generators;

import com.google.common.base.Objects;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateHigherIncomeThanCertainPercentileCommand;
import se.tink.backend.insights.app.queryservices.StatisticsQueryService;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.DateUtils;

public class HigherIncomeThanCertainPercentileGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(HigherIncomeThanCertainPercentileGenerator.class);

    private CommandGateway gateway;
    private StatisticsQueryService statisticsQueryService;
    private UserQueryService userQueryService;

    @Inject
    public HigherIncomeThanCertainPercentileGenerator(CommandGateway gateway,
            StatisticsQueryService statisticsQueryService,
            UserQueryService userQueryService) {
        this.gateway = gateway;
        this.statisticsQueryService = statisticsQueryService;
        this.userQueryService = userQueryService;
    }


    // TODO: clean statistics models
    @Override
    public void generateIfShould(UserId userId) {

        User user = userQueryService.findById(userId);

        //TODO: Determine if currentPeriod or other period should be used
        final String currentPeriod = DateUtils
                .getCurrentMonthPeriod(user.getProfile().getPeriodMode(), user.getProfile().getPeriodAdjustedDay());

        List<Statistic> statistics = statisticsQueryService.getUserStatistics(userId);

        Optional<Statistic> netIncomeCurrentPeriod = statistics.stream()
                .filter(s -> Objects.equal(s.getType(), Statistic.Types.INCOME_NET))
                .filter(s -> Objects.equal(s.getResolution(), user.getProfile().getPeriodMode()))
                .filter(s -> Objects.equal(s.getPeriod(), currentPeriod))
                .findFirst();

        if (!netIncomeCurrentPeriod.isPresent()) {
            log.warn(userId,
                    String.format("No insight generated. Reason: No net-income statistic found for given period: %s ",
                            currentPeriod));
            return;
        }
        // TODO: Need the overall distribution of Tink users net income to see what percentile a user belong to
        // For now - use an arbitrary average income and check whether a user is above the average
        double averageUserNetIncome = 10000.0;
        double netIncome = netIncomeCurrentPeriod.get().getValue();

        if (netIncome <= averageUserNetIncome) {
            log.info(userId, "No insight generated. Reason: Less income than average user income");
            return;
        }

        double percentBetter = ((netIncome - averageUserNetIncome) / averageUserNetIncome) * 100;

        CreateHigherIncomeThanCertainPercentileCommand command = new CreateHigherIncomeThanCertainPercentileCommand(
                userId, percentBetter);
        gateway.on(command);

    }
}

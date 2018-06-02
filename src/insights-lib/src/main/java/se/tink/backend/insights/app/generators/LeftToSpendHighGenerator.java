package se.tink.backend.insights.app.generators;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateLeftToSpendHighInsightCommand;
import se.tink.backend.insights.app.queryservices.StatisticsQueryService;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.DateUtils;

public class LeftToSpendHighGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(LeftToSpendLowGenerator.class);

    private CommandGateway gateway;
    private StatisticsQueryService statisticsQueryService;
    private UserQueryService userQueryService;

    @Inject
    public LeftToSpendHighGenerator(CommandGateway gateway,
            StatisticsQueryService statisticsQueryService, UserQueryService userQueryService) {
        this.gateway = gateway;
        this.statisticsQueryService = statisticsQueryService;
        this.userQueryService = userQueryService;
    }



    @Override
    public void generateIfShould(UserId userId) {

        UserProfile userProfile = userQueryService.getUserProfile(userId);

        List<Statistic> statistics = statisticsQueryService.getUserStatistics(userId);

        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(DateUtils.getToday());

        Optional<Statistic> leftToSpendStatisticForToday = statistics.stream()
                .filter(s -> Objects.equal(s.getType(), Statistic.Types.LEFT_TO_SPEND))
                .filter(s -> Objects.equal(s.getResolution(), userProfile.getPeriodMode()))
                .filter(s -> Objects.equal(s.getDescription(), today)).findFirst();

        if (!leftToSpendStatisticForToday.isPresent()) {
            log.warn(userId, String.format("No left-to-spend statistics found for given date: %s", today));
            return;
        }

        double amount = leftToSpendStatisticForToday.get().getValue();

        if (amount <= 0) {
            log.info(userId, "No insight generated. Reason: 0 or negative amount left to spend");
            return;
        }

        CreateLeftToSpendHighInsightCommand command = new CreateLeftToSpendHighInsightCommand(userId, Amount.of(amount));

        gateway.on(command);
    }
}

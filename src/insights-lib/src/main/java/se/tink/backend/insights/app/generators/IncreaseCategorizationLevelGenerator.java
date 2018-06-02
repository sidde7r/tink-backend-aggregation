package se.tink.backend.insights.app.generators;

import javax.inject.Inject;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateIncreaseCategorizationLevelInsightCommand;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.transactions.TransactionQueryService;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.utils.LogUtils;

public class IncreaseCategorizationLevelGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(IncreaseCategorizationLevelGenerator.class);

    private CommandGateway commandGateway;
    private TransactionQueryService transactionQueryService;
    private UserQueryService userQueryService;

    @Inject
    public IncreaseCategorizationLevelGenerator(
            CommandGateway commandGateway, TransactionQueryService transactionQueryService,
            UserQueryService userQueryService) {
        this.commandGateway = commandGateway;
        this.transactionQueryService = transactionQueryService;
        this.userQueryService = userQueryService;
    }

    @Override
    public void generateIfShould(UserId userId) {

        final double CATEGORIZATION_LEVEL_THRESHOLD = 0.95;

        Long currentLevel = userQueryService.getAmountCategorizationLevel(userId);
        long threshold = (long) (100 * CATEGORIZATION_LEVEL_THRESHOLD);

        // Check if we should suggest transactions or if the current level is high enough
        if (currentLevel != null && currentLevel >= threshold) {
            log.info(userId, "No insight generated. Reason: Current categorization level is high enough");
            return;
        }

        Integer transactionsCount = transactionQueryService.getTransactionsCount(userId);
        // Check if transactions count not null or zero
        if (transactionsCount == null || transactionsCount == 0) {
            log.warn(userId, "No insight generated. Reason: Null or zero transactions found");
            return;
        }

        int numberOfNonCategorized = (int) Math
                .ceil(transactionsCount * (CATEGORIZATION_LEVEL_THRESHOLD - currentLevel/100));

        CreateIncreaseCategorizationLevelInsightCommand command = new CreateIncreaseCategorizationLevelInsightCommand(
                userId, numberOfNonCategorized, (int) (long) currentLevel);
        commandGateway.on(command);

    }
}

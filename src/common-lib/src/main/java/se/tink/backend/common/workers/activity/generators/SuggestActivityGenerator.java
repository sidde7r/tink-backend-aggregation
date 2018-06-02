package se.tink.backend.common.workers.activity.generators;

import java.util.List;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.search.SuggestTransactionsSearcher;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class SuggestActivityGenerator extends ActivityGenerator {
    private static final LogUtils log = new LogUtils(SuggestActivityGenerator.class);
    private static final double CATEGORIZATION_LEVEL_THRESHOLD = 0.95;
    private static final double CATEGORIZATION_IMPROVEMENT_THRESHOLD = 0.01;
    private static final int DEFAULT_NUMBER_OF_CLUSTERS = 7;
    private MetricRegistry metricRegistry;

    public SuggestActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory,
            MetricRegistry metricRegistry) {
        super(SuggestActivityGenerator.class, 70, 90, deepLinkBuilderFactory);
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        SuggestTransactionsResponse suggestedTransactions = suggestTransactions(context);

        if (suggestedTransactions == null
                || suggestedTransactions.getCategorizationLevel() > CATEGORIZATION_LEVEL_THRESHOLD
                || suggestedTransactions.getCategorizationImprovement() < CATEGORIZATION_IMPROVEMENT_THRESHOLD) {
            return;
        }

        String key = "suggest-category";

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        context.addActivity(
                createActivity(
                        context.getUser().getId(),
                        DateUtils.getToday(),
                        Activity.Types.SUGGEST,
                        null,
                        null,
                        suggestedTransactions,
                        key,
                        feedActivityIdentifier));
    }

    private SuggestTransactionsResponse suggestTransactions(ActivityGeneratorContext context) {

        User user = context.getUser();
        UserState userState = context.getUserState();
        List<Transaction> transactions = context.getTransactions();
        List<Account> accounts = context.getAccounts();

        try {

            Long currentLevel = userState.getAmountCategorizationLevel();

            long threshold = (long) (100 * CATEGORIZATION_LEVEL_THRESHOLD);

            // Check if we should suggest transactions or if the current level is high enough
            if (currentLevel != null && currentLevel >= threshold) {
                log.debug(user.getId(), "Not suggesting transactions since level is equal or above threshold.");
                return null;
            }

            return new SuggestTransactionsSearcher(context.getServiceContext(), context.getCurrencyByCode(),
                    metricRegistry)
                    .suggest(user, DEFAULT_NUMBER_OF_CLUSTERS, true, transactions, accounts);

        } catch (Exception e) {
            log.error(user.getId(), "Caught exception while suggesting transactions", e);
        }

        return null;
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}

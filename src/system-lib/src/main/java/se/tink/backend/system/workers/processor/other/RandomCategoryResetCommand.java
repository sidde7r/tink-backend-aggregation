package se.tink.backend.system.workers.processor.other;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Randomly resets the category for transactions in order to entice the user to categorize transactions herself.
 */
public class RandomCategoryResetCommand implements TransactionProcessorCommand {
    private static final LogUtils log = new LogUtils(RandomCategoryResetCommand.class);
    private static final MetricId RANDOMLY_RESET_COUNTER_NAME = MetricId.newId("transactions_category_randomly_reset");

    private final Counter randomlyResetCategoryCounter;
    private final Category unknownExpenseCategory;
    private final CategoryChangeRecordDao categoryChangeRecordsDao;
    private final double uncategorizationProbability;

    public RandomCategoryResetCommand(ClusterCategories categories, CategoryConfiguration categoryConfiguration,
            MetricRegistry metricRegistry, CategoryChangeRecordDao categoryChangeRecordsDao,
            User user, CategorizationConfiguration categorizationConfiguration
    ) {
        this.unknownExpenseCategory = categories.get().stream()
                .filter(c -> categoryConfiguration.getExpenseUnknownCode().equals(c.getCode()))
                .findFirst()
                .get();

        randomlyResetCategoryCounter = metricRegistry.meter(RANDOMLY_RESET_COUNTER_NAME);
        this.categoryChangeRecordsDao = categoryChangeRecordsDao;

        Double marketUncategorizationProbability = categorizationConfiguration.getMarketToUncategorizationProbability()
                .get(user.getProfile().getMarketAsCode());
        uncategorizationProbability = marketUncategorizationProbability == null ?
                categorizationConfiguration.getDefaultUncategorizationProbability() :
                marketUncategorizationProbability;
        log.info("Setting random uncategorization probability to: " + uncategorizationProbability);
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        // Skip transactions where the category has been set by the user
        if (transaction.isUserModifiedCategory()) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        // Only reset expenses
        if (transaction.getAmount() > 0) {
            return TransactionProcessorCommandResult.CONTINUE;
        }

        // Reset the category on random transactions based on the UNCATEGORIZATION_PROBABILITY.
        if (Math.random() < uncategorizationProbability) {
            log.debug(transaction.getUserId(),
                    "Randomly reset the category for transaction: " + transaction.getDescription());
            randomlyResetCategoryCounter.inc();

            // TODO: Not modify incoming transaction. By definition, a Classifier should not modify input
            //       parameters.
            transaction.setCategoryId(unknownExpenseCategory.getId());
            // Not setting CategoryType on transaction since we are expecting that to already have been set.

            categoryChangeRecordsDao.save(CategoryChangeRecord.createChangeRecord(transaction,
                    Optional.of(unknownExpenseCategory.getId()), this.toString()));
        }

        return TransactionProcessorCommandResult.CONTINUE;
    }

    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    /**
     * Called for every command in command chain's reverse order at after processing all transactions.
     */
    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("unknownExpenseCategory", unknownExpenseCategory)
                .toString();
    }
}

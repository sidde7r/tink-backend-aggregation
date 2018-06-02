package se.tink.backend.system.workers.processor.categorization;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.categorization.interfaces.Categorizer;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;

public class CategorizerCommand implements TransactionProcessorCommand {
    private static final LogUtils log = new LogUtils(CategorizerCommand.class);

    private final Categorizer categorizer;
    private final CategoryChangeRecordDao categoryChangeRecordDao;

    public CategorizerCommand(Categorizer categorizer, CategoryChangeRecordDao categoryChangeRecordDao) {
        this.categorizer = categorizer;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
    }

    @Override
    public TransactionProcessorCommandResult initialize() {
        return TransactionProcessorCommandResult.CONTINUE;
    }

    @Override
    public TransactionProcessorCommandResult execute(Transaction transaction) {
        run(transaction);
        return TransactionProcessorCommandResult.CONTINUE;
    }

    private void run(Transaction transaction) {
        // Skip transactions where the category has been set by the user
        if (transaction.isUserModifiedCategory()) {
            return;
        }

        Optional<String> oldCategory = Optional.ofNullable(transaction.getCategoryId());

        Category category = categorizer.categorize(transaction);
        transaction.setCategory(category);

        categoryChangeRecordDao.save(CategoryChangeRecord.createChangeRecord(transaction,
                oldCategory, this.toString()), categoryChangeRecordDao.CATEGORY_CHANGE_RECORD_TTL_DAYS, TimeUnit.DAYS);

        return;
    }

    @Override
    public void postProcess() {
        // Deliberately left empty.
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("categorizer", categorizer)
                .toString();
    }
}

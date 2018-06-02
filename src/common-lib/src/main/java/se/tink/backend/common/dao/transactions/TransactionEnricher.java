package se.tink.backend.common.dao.transactions;

import com.google.inject.Inject;
import java.util.Date;
import java.util.Map;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

public class TransactionEnricher {
    private static final String DEFAULT_LOCALE = "sv_SE";
    private CategoryRepository categoryRepository;
    private static final LogUtils log = new LogUtils(TransactionEnricher.class);

    @Inject
    public TransactionEnricher(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void enrich(Transaction transaction) {
        // Automatically set the transactions category type based on the
        // category id.

        if (transaction.getCategoryId() != null) {
            try {
                Map<String, Category> cache = categoryRepository.getCategoriesById(DEFAULT_LOCALE);
                Category category = cache.get(transaction.getCategoryId());
                transaction.setCategoryType(category.getType());
            } catch (NullPointerException e) {
                log.error(transaction.getUserId(),
                        String.format("Could not find category for categoryId:%s for transaction inserted:%s",
                        transaction.getCategoryId(), transaction.getInserted()), e);
                throw e;
            }
        }

        transaction.setLastModified(new Date());
    }
}

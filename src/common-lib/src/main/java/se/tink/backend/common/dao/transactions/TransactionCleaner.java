package se.tink.backend.common.dao.transactions;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

/**
 * Utility class that is used to make sure we always set transaction category to a leaf node category.
 */
public class TransactionCleaner {

    private final CategoryRepository categoryRepository;
    private static final LogUtils log = new LogUtils(TransactionCleaner.class);

    @Inject
    public TransactionCleaner(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Return a replacement category if there is a better one.
     * @param originalCategoryId the original category.
     * @return
     */
    public Optional<Category> getReplacementCategory(String originalCategoryId) {
        Category category = categoryRepository.findById(Preconditions.checkNotNull(originalCategoryId));
        
        // A way to get the categories that are to be cleaned is by issuing:
        // SELECT id, code, primaryname, secondaryname FROM categories WHERE code NOT LIKE '%.%' or code LIKE '%other%'
        // ORDER BY code;

        if (category.getCode().contains(".")) {
            return Optional.empty();
        }

        log.info(String.format("Returning replacement category for %s", category.getCode()));

        return Optional.ofNullable(categoryRepository.findByCode(category.getCode() + ".other"));
    }

    public boolean clean(Transaction transaction) {
        Optional<Category> newCategory = getReplacementCategory(transaction.getCategoryId());
        if (newCategory.isPresent()) {
            transaction.setCategory(newCategory.get());
            return true;
        }
        return false;
    }
    
}

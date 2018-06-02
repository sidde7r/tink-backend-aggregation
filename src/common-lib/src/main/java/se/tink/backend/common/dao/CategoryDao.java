package se.tink.backend.common.dao;

import com.google.inject.Inject;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;

public class CategoryDao {
    private final CategoryRepository categoryRepository;
    private final CategoryConfiguration categoryConfiguration;

    @Inject
    public CategoryDao(CategoryRepository categoryRepository,
            CategoryConfiguration categoryConfiguration) {
        this.categoryRepository = categoryRepository;
        this.categoryConfiguration = categoryConfiguration;
    }

    public Category getUnknownExpense() {
        return categoryRepository.findByCode(categoryConfiguration.getExpenseUnknownCode());
    }
}

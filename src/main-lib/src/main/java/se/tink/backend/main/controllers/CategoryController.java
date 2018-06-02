package se.tink.backend.main.controllers;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;

import javax.annotation.Nonnull;

public class CategoryController {
    @VisibleForTesting static final String DEFAULT_LOCALE = "en_US";
    private final CategoryRepository categoryRepository;

    @Inject
    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> list(@Nonnull String locale) {
        return list(Optional.of(locale));
    }

    public List<Category> list(Optional<String> locale) {
        return categoryRepository.findAll(locale.orElse(DEFAULT_LOCALE));
    }

    public Map<String, Category> localeCategoriesToIds(String locale) {
        return categoryRepository.getCategoriesById(locale);
    }
}

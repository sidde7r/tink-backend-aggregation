package se.tink.backend.common.statistics;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Predicate;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.guavaimpl.predicates.CategoriesFromPresetCodes;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;

public class TransactionBasedOnCategory implements Predicate<Transaction> {

    private Set<String> categoryIdsToInclude;

    public TransactionBasedOnCategory(final Collection<Category> allCategories, final Set<String> codesToInclude) {

        Iterable<Category> categories = Iterables.filter(allCategories, new CategoriesFromPresetCodes(codesToInclude));
        Iterable<String> ids = Iterables.transform(categories, Category::getId);

        categoryIdsToInclude = Sets.newHashSet(ids);
    }

    @Override
    public boolean apply(@Nullable Transaction t) {
        return categoryIdsToInclude.contains(t.getCategoryId());
    }
}

package se.tink.backend.utils.guavaimpl.predicates;

import java.util.Set;

import javax.annotation.Nullable;

import se.tink.backend.core.Category;

import com.google.common.base.Predicate;

public class CategoriesFromPresetCodes implements Predicate<Category> {

    private Set<String> codesToInclude;

    public CategoriesFromPresetCodes(Set<String> codesToInclude) {
        this.codesToInclude = codesToInclude;
    }

    @Override
    public boolean apply(@Nullable Category category) {
        return codesToInclude.contains(category.getCode());
    }
}

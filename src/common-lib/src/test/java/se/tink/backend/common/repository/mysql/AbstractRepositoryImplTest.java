package se.tink.backend.common.repository.mysql;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTranslation;

import java.util.Collections;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class AbstractRepositoryImplTest {

    @Test
    public void translateCategories() {
        Category category = new Category();
        category.setCode("categoryCode");
        category.setPrimaryName("primaryName");
        CategoryTranslation translation = new CategoryTranslation();
        translation.setPrimaryName("translatedPrimaryName");

        AbstractRepositoryImpl.translateCategories(
                () -> ImmutableMap.of("locale", ImmutableMap.of("categoryCode", translation)),
                singletonList(category), "locale");
        assertEquals("translatedPrimaryName", category.getPrimaryName());
    }

    @Test
    public void translateWhenNoLocaleTranslations() {
        Category category = new Category();
        category.setPrimaryName("primaryName");

        AbstractRepositoryImpl.translateCategories(Collections::emptyMap, singletonList(category), "locale");
        assertEquals("primaryName", category.getPrimaryName());
    }

    @Test
    public void translateWhenNoCategoryTranslation() {
        Category category = new Category();
        category.setPrimaryName("primaryName");

        AbstractRepositoryImpl.translateCategories(
                () -> ImmutableMap.of("locale", emptyMap()),
                singletonList(category), "locale");
        assertEquals("primaryName", category.getPrimaryName());
    }

}

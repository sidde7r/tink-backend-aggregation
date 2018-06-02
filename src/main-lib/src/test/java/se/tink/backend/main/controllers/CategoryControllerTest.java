package se.tink.backend.main.controllers;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.categorization.api.AbnAmroCategories;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.main.TestUtils;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class CategoryControllerTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryController categoryController;

    private Category seCategory = TestUtils.createCategory(SECategories.Codes.EXPENSES_FOOD);
    private Category nlCategory = TestUtils.createCategory(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY);

    @Test
    public void listCategoriesByLocale() {
        List<Category> categories = singletonList(new Category());
        when(categoryRepository.findAll("locale")).thenReturn(categories);

        assertEquals(categories, categoryController.list("locale"));
    }

    @Test
    public void listCategoriesByOptionalLocale() {
        List<Category> locales = singletonList(new Category());
        when(categoryRepository.findAll("locale")).thenReturn(locales);

        assertEquals(locales, categoryController.list(Optional.of("locale")));
    }

    @Test
    public void listCategoriesByDefaultLocale() {
        List<Category> locales = singletonList(new Category());
        when(categoryRepository.findAll(CategoryController.DEFAULT_LOCALE)).thenReturn(locales);

        assertEquals(locales, categoryController.list(Optional.empty()));
    }

    @Test
    @Parameters({
            "SE, expenses:food",
            "NL, expenses:entertainment.hobby"
    })
    public void getCategoryMapByLocale(String locale, String categoryCode) {
        String mapKey = "mapKey";
        when(categoryRepository.getCategoriesById("SE")).thenReturn(ImmutableMap.of(mapKey, seCategory));
        when(categoryRepository.getCategoriesById("NL")).thenReturn(ImmutableMap.of(mapKey, nlCategory));

        Map<String, Category> map = categoryController.localeCategoriesToIds(locale);
        assertEquals(1, map.size());
        assertEquals(categoryCode, map.get(mapKey).getCode());
    }

    @Test
    public void getCategoryMapByNonExistedLocale() {
        String mapKey = "mapKey";
        when(categoryRepository.getCategoriesById("SE")).thenReturn(ImmutableMap.of(mapKey, seCategory));
        when(categoryRepository.getCategoriesById("NL")).thenReturn(ImmutableMap.of(mapKey, nlCategory));

        Map<String, Category> map = categoryController.localeCategoriesToIds("FR");
        assertTrue(map.isEmpty());
    }

}

package se.tink.backend.grpc.v1.utils;

import org.junit.Test;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Category;
import se.tink.backend.grpc.v1.converter.category.CategoryNode;
import se.tink.backend.grpc.v1.converter.category.CategoryTree;
import static org.assertj.core.api.Assertions.assertThat;

public class CategoryTreeBuilderTest {

    private Category createCategory(String categoryCode) {
        Category category = new Category();
        category.setCode(categoryCode);
        return category;
    }

    @Test
    public void buildCategoryTree() {
        CategoryTree categoryTree = new CategoryTreeBuilder()
                .addNode(createCategory(SECategories.Codes.EXPENSES_FOOD_COFFEE))
                .addNode(createCategory(SECategories.Codes.EXPENSES_FOOD))
                .addNode(createCategory(SECategories.Codes.EXPENSES_FOOD_OTHER))
                .build();

        assertThat(categoryTree.getExpenses()).isNotNull();
        assertThat(categoryTree.getIncome()).isNull();
        assertThat(categoryTree.getTransfers()).isNull();

        CategoryNode expenses = categoryTree.getExpenses();
        assertThat(expenses.getChildren()).containsOnlyKeys("food");

        CategoryNode foodCategory = expenses.getChildren().get("food");
        assertThat(foodCategory.getChildren()).containsOnlyKeys("coffee", "other");
    }
}

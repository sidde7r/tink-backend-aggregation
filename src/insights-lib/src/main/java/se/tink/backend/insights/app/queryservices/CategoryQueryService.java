package se.tink.backend.insights.app.queryservices;

import java.util.List;
import java.util.Set;
import se.tink.backend.core.Category;

public interface CategoryQueryService {

    public List<Category> getAllCategories();

    public Category findById(String id);

    public Category findByCode(String code);

    public String getCategoryDisplayName(String id);

    public String getUnknownCategoryExpensesId(String locale);

    public Set<String> getUninterestingMonthExpenseCategoryIds();

    public String getTransfersExcludeOtherId();
}

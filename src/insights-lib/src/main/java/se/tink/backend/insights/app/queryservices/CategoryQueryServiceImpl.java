package se.tink.backend.insights.app.queryservices;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;

public class CategoryQueryServiceImpl implements CategoryQueryService {

    private CategoryConfiguration configuration;
    private CategoryRepository categoryRepository;

    @Inject
    public CategoryQueryServiceImpl(CategoryConfiguration configuration,
            CategoryRepository categoryRepository) {
        this.configuration = configuration;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    @Override
    public Category findById(String id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category findByCode(String code){
        return categoryRepository.findByCode(code);
    }

    @Override
    public String getCategoryDisplayName(String id){
        return categoryRepository.findById(id).getDisplayName();
    }

    @Override
    public String getUnknownCategoryExpensesId(String locale){
        return categoryRepository.findByCode(configuration.getExpenseUnknownCode(), locale).getId();
    }

    @Override
    public Set<String> getUninterestingMonthExpenseCategoryIds() {
        Set<String> categoryIds = Sets.newHashSet();
        categoryIds.add(categoryRepository.findByCode(configuration.getRentCode()).getId());
        categoryIds.add(categoryRepository.findByCode(configuration.getMortgageCode()).getId());
        return categoryIds;
    }

    @Override
    public String getTransfersExcludeOtherId(){
        return categoryRepository.findByCode(configuration.getExcludeCode()).getId();
    }
}

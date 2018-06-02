package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import java.util.Map;

import se.tink.backend.core.Category;

public interface CategoryRepositoryCustom {
    public List<Category> findAll();

    public List<Category> findAll(String locale);

    public Category findByCode(String code);

    public Category findByCode(String code, String locale);

    public List<Category> findLeafCategories();

    public List<Category> findLeafCategories(String locale);
    
    public Map<String, Category> getCategoriesById(String locale);
    
    public String getDefaultLocale();

    Category findById(String id);

    Category findById(String id, String locale);
}

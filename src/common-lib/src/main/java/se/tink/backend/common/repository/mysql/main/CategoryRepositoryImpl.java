package se.tink.backend.common.repository.mysql.main;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.tink.backend.common.repository.mysql.AbstractRepositoryImpl;
import se.tink.backend.core.Category;

public class CategoryRepositoryImpl extends AbstractRepositoryImpl implements CategoryRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Category> findAll() {
        return findCategories(DEFAULT_LOCALE);
    }

    @Override
    public List<Category> findAll(String locale) {
        return findCategories(locale);
    }

    @Override
    public Category findByCode(String code) {
        return findByCode(code, DEFAULT_LOCALE);
    }

    @Override
    public Category findByCode(final String code, String locale) {
        return findCategories(locale).stream().filter(c -> Objects.equal(code, c.getCode())).findFirst().orElse(null);
    }

    @Override
    public List<Category> findLeafCategories() {
        return findLeafCategories(DEFAULT_LOCALE);
    }

    @Override
    public List<Category> findLeafCategories(String locale) {
        return Lists.newArrayList(Iterables.filter(findCategories(locale),
                c -> (!Strings.isNullOrEmpty(c.getSecondaryName()))));
    }

    // Expire after some time to allow for category modifications to eventually propagate through.
    private LoadingCache<String, Map<String, Category>> categoriesByIdCache = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES).build(new CacheLoader<String, Map<String, Category>>() {

                @Override
                public Map<String, Category> load(String locale) throws Exception {
                    return Maps.uniqueIndex(findAll(locale), Category::getId);
                }

            });

    @Override
    public Map<String, Category> getCategoriesById(String locale) {
        try {
            return categoriesByIdCache.get(locale);
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get categories by locale.", e);
        }
    }

    @Override
    public String getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    @Override
    public Category findById(String id) {
        return findById(id, DEFAULT_LOCALE);
    }

    @Override
    public Category findById(String id, String locale) {
        try {
            return categoriesByIdCache.get(locale).get(id);
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get categories by locale.", e);
        }
    }
}

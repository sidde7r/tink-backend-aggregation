package se.tink.backend.common.repository.mysql;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTranslation;

public class AbstractRepositoryImpl {
    public static final String DEFAULT_LOCALE = "sv_SE";
    // Expire after 20 minutes to allow for category modifications to eventually propagate through.
    private final LoadingCache<String, List<Category>> categoriesByLocaleCache = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES).build(new CacheLoader<String, List<Category>>() {

                @Override
                public List<Category> load(String locale) throws Exception {
                    List<Category> categories = em.createQuery("select c from Category c", Category.class)
                            .getResultList();

                    translateCategories(categoriesTranslationsByCodeByLocale, categories, locale);

                    return categories;
                }

            });

    private Supplier<Map<String, Map<String, CategoryTranslation>>> categoriesTranslationsByCodeByLocale = Suppliers
            .memoizeWithExpiration(new Supplier<Map<String, Map<String, CategoryTranslation>>>() {

                @Override
                public Map<String, Map<String, CategoryTranslation>> get() {
                    Map<String, Map<String, CategoryTranslation>> result = Maps.newHashMap();

                    ImmutableListMultimap<String, CategoryTranslation> translationsBylanguage = Multimaps.index(em
                            .createQuery("select ct from CategoryTranslation ct", CategoryTranslation.class)
                            .getResultList(), CategoryTranslation::getLocale);

                    for (String languageKey : translationsBylanguage.keySet()) {
                        result.put(languageKey, Maps.uniqueIndex(translationsBylanguage.get(languageKey),
                                CategoryTranslation::getCode));
                    }

                    return result;
                }

            }, 30, TimeUnit.MINUTES);

    @PersistenceContext
    private EntityManager em;

    protected List<Category> findCategories(String locale) {
        try {
            return categoriesByLocaleCache.get(locale);
        } catch (ExecutionException e) {
            // Throwing RuntimeException to avoid checked exception that'll trickle down throughout call hierarchy.
            throw new RuntimeException("Could not get categories for locale '" + locale + " from cache'.", e);
        }
    }

    @VisibleForTesting
    static void translateCategories(
            Supplier<Map<String, Map<String, CategoryTranslation>>> translationsSupplier,
            List<Category> categories, String locale) {
        // Save to temporary variable to make sure we are working on the same copy throughout the loop.
        Map<String, Map<String, CategoryTranslation>> categoriesTranslationsByCodeByLocale = translationsSupplier.get();

        categories.forEach(category ->
                Optional.ofNullable(categoriesTranslationsByCodeByLocale.get(locale))
                        .flatMap(translations -> Optional.ofNullable(translations.get(category.getCode())))
                        .ifPresent(translation -> {
                            category.setTypeName(translation.getTypeName());
                            category.setPrimaryName(translation.getPrimaryName());
                            category.setSecondaryName(translation.getSecondaryName());
                            category.setSearchTerms(translation.getSearchTerms());
                        })
        );
    }
}

package se.tink.backend.common.search.parsers;

import java.util.HashMap;
import java.util.List;

import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;

import se.tink.backend.common.search.SearchParserCommand;
import se.tink.backend.common.search.SearchParserContext;
import se.tink.backend.core.Category;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CategorySearchParserCommand extends SearchParserCommand {
    private static final Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();
    private static HashMap<String, HashMap<String, String>> categoryIdsMapsPerLocale = Maps.newHashMap();
    private static HashMap<String, HashMap<String, String>> categoryParentIdsMapsPerLocale = Maps.newHashMap();
    private static HashMap<String, HashMap<String, String>> categoryTypeNamesMapsPerLocale = Maps.newHashMap();

    @Override
    public List<String> parse(List<String> queryWords, SearchParserContext context, boolean addQuery) {
        List<String> resultingQueryWords = Lists.newArrayList();
        HashMap<String, String> categoryIdsBySecondaryName = getCategoryIdsMap(context);
        HashMap<String, String> categoryParentIdsByPrimaryName = getCategoryParentIdsMap(context);
        HashMap<String, String> categoryTypeNames = getCategoryTypeNamesMap(context);

        for (int i = 0; i < queryWords.size(); i++) {
            String word = queryWords.get(i).toLowerCase().replace(",", "");

            if (categoryTypeNames.containsKey(word)) {
                if (addQuery) {
                    context.getQueryFilters().add(
                            FilterBuilders.orFilter(
                                    FilterBuilders.termFilter("category.type", categoryTypeNames.get(word)),
                                    FilterBuilders.queryFilter(QueryBuilders.queryString(word).field("description")
                                            .useDisMax(true))));
                }

                continue;
            } else if (categoryParentIdsByPrimaryName.containsKey(word)) {
                if (addQuery) {
                    context.getQueryFilters().add(
                            FilterBuilders.orFilter(
                                    FilterBuilders.termFilter("category.parent",
                                            categoryParentIdsByPrimaryName.get(word)),
                                    FilterBuilders.queryFilter(QueryBuilders.queryString(word).field("description")
                                            .useDisMax(true))));
                }

                continue;
            } else if (categoryIdsBySecondaryName.containsKey(word)) {
                if (addQuery) {
                    context.getQueryFilters().add(
                            FilterBuilders.orFilter(
                                    FilterBuilders.termFilter("category.id", categoryIdsBySecondaryName.get(word)),
                                    FilterBuilders.queryFilter(QueryBuilders.queryString(word).field("description")
                                            .useDisMax(true))));
                }

                continue;
            }

            resultingQueryWords.add(word);
        }

        return resultingQueryWords;
    }

    private HashMap<String, String> getCategoryTypeNamesMap(SearchParserContext context) {
        String locale = context.getUser().getProfile().getLocale();

        if (categoryTypeNamesMapsPerLocale.get(locale) == null) {

            HashMap<String, String> typeNamesMap = Maps.newHashMap();
            for (Category c : context.getCategories()) {
                typeNamesMap.put(c.getTypeName().toLowerCase(), c.getType().toString());
            }
            categoryTypeNamesMapsPerLocale.put(locale, typeNamesMap);
        }

        return categoryTypeNamesMapsPerLocale.get(locale);
    }

    private HashMap<String, String> getCategoryParentIdsMap(SearchParserContext context) {
        String locale = context.getUser().getProfile().getLocale();

        if (categoryParentIdsMapsPerLocale.get(locale) == null) {
            HashMap<String, String> parentIdsMap = Maps.newHashMap();

            for (Category c : context.getCategories()) {
                if (c.getPrimaryName() != null && c.isDefaultChild()) {
                    for (String name : SPLITTER.split(c.getPrimaryName().replace("&", "").replace(",", ""))) {
                        parentIdsMap.put(name.toLowerCase(), c.getParent());
                    }
                }
            }

            categoryParentIdsMapsPerLocale.put(locale, parentIdsMap);
        }

        return categoryParentIdsMapsPerLocale.get(locale);
    }

    private HashMap<String, String> getCategoryIdsMap(SearchParserContext context) {
        String locale = context.getUser().getProfile().getLocale();

        if (categoryIdsMapsPerLocale.get(locale) == null) {
            HashMap<String, String> idsMap = Maps.newHashMap();

            for (Category c : context.getCategories()) {
                if (c.getSecondaryName() != null && !c.isDefaultChild()) {
                    for (String name : SPLITTER.split(c.getSecondaryName().replace("&", "").replace(",", ""))) {
                        idsMap.put(name.toLowerCase(), c.getId());
                    }
                }
            }

            categoryIdsMapsPerLocale.put(locale, idsMap);
        }

        return categoryIdsMapsPerLocale.get(locale);
    }
}

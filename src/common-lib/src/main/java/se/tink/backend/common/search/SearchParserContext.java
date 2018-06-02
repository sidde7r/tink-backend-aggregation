package se.tink.backend.common.search;

import java.util.List;
import java.util.Locale;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.Category;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.User;

public class SearchParserContext {
    private Catalog catalog;
    private Locale locale;
    private QueryBuilder queryBuilder;
    private List<FilterBuilder> queryFilters;
    private SearchQuery searchQuery;
    private User user;
    private List<Category> categories;
    private ResolutionTypes responseResolution; 

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public List<FilterBuilder> getQueryFilters() {
        return queryFilters;
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public User getUser() {
        return user;
    }

    public void setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public void setQueryFilters(List<FilterBuilder> queryFilters) {
        this.queryFilters = queryFilters;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public ResolutionTypes getResponseResolution() {
        return responseResolution;
    }

    public void setResponseResolution(ResolutionTypes resolution) {
        this.responseResolution = resolution;
    }
}

package se.tink.backend.common.workers.activity.renderers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.libraries.date.Period;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;

public class ActivityRendererContext {

    private List<Category> categories;
    private List<Credentials> credentials;
    private List<Account> accounts;
    private User user;
    private Catalog catalog;
    private Map<String, Currency> currencies;
    private double screenWidth;
    private double screenResolution;
    private Theme theme;
    private PooledRythmProxy templateRenderer;
    private List<Period> cleanPeriods;
    private TinkUserAgent userAgent;
    private CategoryConfiguration categoryConfiguration;
    private Cluster cluster;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category getExpenseCategory() {
        for (Category c : categories) {
            if (c.getCode().equals("expenses")) {
                return c;
            }
        }
        return null;
    }

    public Category getIncomeCategory() {
        for (Category c : categories) {
            if (c.getCode().equals("income")) {
                return c;
            }
        }
        return null;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Map<String, Currency> currencies) {
        this.currencies = currencies;
    }

    public Currency getUserCurrency() {
        return currencies.get(user.getProfile().getCurrency());
    }

    public Locale getLocale() {
        return Catalog.getLocale(user.getProfile().getLocale());
    }

    public Category getCategory(String credentialsId) {
        for (Category c : categories) {
            if (c.getId().equals(credentialsId)) {
                return c;
            }
        }
        return null;
    }

    public Category getCategoryFromCode(String categoryCode) {
        for (Category c : categories) {
            if (c.getCode().equals(categoryCode)) {
                return c;
            }
        }
        return null;
    }

    public double getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(double screenWidth) {
        this.screenWidth = screenWidth;
    }

    public double getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(double screenResolution) {
        this.screenResolution = screenResolution;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Account getAccount(String accountId) {
        for (Account a : accounts) {
            if (a.getId().equals(accountId)) {
                return a;
            }
        }
        return null;
    }

    public PooledRythmProxy getTemplateRenderer() {
        return templateRenderer;
    }

    public void setTemplateRenderer(PooledRythmProxy templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    public List<Period> getCleanPeriods() {
        return cleanPeriods;
    }

    public void setCleanPeriods(List<Period> cleanPeriods) {
        this.cleanPeriods = cleanPeriods;
    }

    public void setUserAgent(TinkUserAgent userAgent) {
        this.userAgent = userAgent;
    }

    public TinkUserAgent getUserAgent() {
        return userAgent;
    }

    public CategoryConfiguration getCategoryConfiguration() {
        return categoryConfiguration;
    }

    public void setCategoryConfiguration(CategoryConfiguration categoryConfiguration) {
        this.categoryConfiguration = categoryConfiguration;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return cluster;
    }
}

package se.tink.backend.common.workers.fraud;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.repository.mysql.main.CompanyEngagementRepository;
import se.tink.backend.common.repository.mysql.main.CompanyRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;

public class FraudDataProcessorContext {

    private List<FraudItem> inStoreFraudItems;
    private List<FraudDetails> inStoreFraudDetails;
    private List<FraudDetails> inBatchFraudDetails = Lists.newArrayList();
    private List<FraudDetails> fraudDetailsRemoveList = Lists.newArrayList();
    private List<FraudDetails> fraudDetailsUpdateList = Lists.newArrayList();
    private List<FraudDetailsContent> fraudDetailsContent = Lists.newArrayList();
    private Set<String> usedTransactionIds = Sets.newHashSet();
    private User user;
    private Map<String, Transaction> transactionsById;
    private List<Activity> activities;
    private List<Credentials> credentials;
    private Currency userCurrency;
    private List<Provider> providers;
    private Map<String, Account> accountsById;
    private Map<String, Category> categoriesByCodeForLocale;
    private CompanyEngagementRepository companyEmgagementRepository;
    private CompanyRepository companyRepository;
    private CategoryConfiguration categoryConfiguration;
    
    public List<FraudItem> getInStoreFraudItems() {
        return inStoreFraudItems;
    }

    public void setInStoreFraudItems(List<FraudItem> fraudItems) {
        this.inStoreFraudItems = fraudItems;
    }

    public List<FraudDetails> getInStoreFraudDetails() {
        return inStoreFraudDetails;
    }

    public void setInStoreFraudDetails(List<FraudDetails> fraudDetails) {
        this.inStoreFraudDetails = fraudDetails;
    }

    public List<FraudDetailsContent> getFraudDetailsContent() {
        return Lists.newArrayList(fraudDetailsContent);
    }

    public void setFraudDetailsContent(List<FraudDetailsContent> fraudDetailsContent) {
        usedTransactionIds = Sets.newHashSet();
        this.fraudDetailsContent = Lists.newArrayList();
        addFraudDetailsContent(fraudDetailsContent);
    }

    public void addFraudDetailsContent(List<FraudDetailsContent> fraudDetailsContent) {

        for (FraudDetailsContent fraudDetails : fraudDetailsContent) {
            if (fraudDetails instanceof FraudTransactionContent) {
                List<String> transactionIds = ((FraudTransactionContent) fraudDetails).getTransactionIds();
                if (transactionIds == null) {
                    continue;
                }
                if (hasTransactionBeenUsedForWarning(transactionIds)) {
                    return;
                }
                usedTransactionIds.addAll(transactionIds);
            }
        }

        this.fraudDetailsContent.addAll(fraudDetailsContent);
    }

    private boolean hasTransactionBeenUsedForWarning(List<String> transactionsIds) {
        for (String id : transactionsIds) {
            if (usedTransactionIds.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<FraudDetails> getInBatchFraudDetails() {
        return inBatchFraudDetails;
    }

    public void setInBatchFraudDetails(List<FraudDetails> newFraudDetails) {
        this.inBatchFraudDetails = newFraudDetails;
    }

    public Map<String, Transaction> getTransactionsById() {
        return transactionsById;
    }

    public void setTransactionsById(Map<String, Transaction> transactionsById) {
        this.transactionsById = transactionsById;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public Currency getUserCurrency() {
        return userCurrency;
    }

    public void setUserCurrency(Currency userCurrency) {
        this.userCurrency = userCurrency;
    }

    public List<FraudDetails> getFraudDetailsRemoveList() {
        return fraudDetailsRemoveList;
    }

    public void setFraudDetailsRemoveList(List<FraudDetails> fraudDetailsRemoveList) {
        this.fraudDetailsRemoveList = fraudDetailsRemoveList;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    public Map<String, Account> getAccountsById() {
        return accountsById;
    }

    public void setAccountsById(Map<String, Account> accountsById) {
        this.accountsById = accountsById;
    }
    
    public Map<String, Category> getCategoriesByCodeForLocale() {
        return categoriesByCodeForLocale;
    }
    
    public void setCategoriesByCodeForLocale(Map<String, Category> categoriesByCodeForLocale) {
        this.categoriesByCodeForLocale = categoriesByCodeForLocale;
    }

    public CompanyRepository getCompanyRepository() {
        return companyRepository;
    }

    public void setCompanyRepository(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyEngagementRepository getCompanyEmgagementRepository() {
        return companyEmgagementRepository;
    }

    public void setCompanyEmgagementRepository(CompanyEngagementRepository companyEmgagementRepository) {
        this.companyEmgagementRepository = companyEmgagementRepository;
    }

    public List<FraudDetails> getFraudDetailsUpdateList() {
        return fraudDetailsUpdateList;
    }

    public void setFraudDetailsUpdateList(List<FraudDetails> fraudDetailsUpdateList) {
        this.fraudDetailsUpdateList = fraudDetailsUpdateList;
    }

    public CategoryConfiguration getCategoryConfiguration() {
        return categoryConfiguration;
    }

    public void setCategoryConfiguration(CategoryConfiguration categoryConfiguration) {
        this.categoryConfiguration = categoryConfiguration;
    }

    @VisibleForTesting
    Set<String> getUsedTransactionIds() {
        return usedTransactionIds;
    }
}

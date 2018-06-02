package se.tink.backend.core;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.property.Property;

public class UserData {
    private List<Account> accounts;
    private List<AccountBalance> accountBalanceHistory;
    private List<CredentialsEvent> credentialsEvents;
    private List<Credentials> credentials;
    private List<FollowItem> followItems;
    private ImmutableListMultimap<String, Loan> loanDataByAccount;
    private List<Statistic> statistics;
    private List<Transaction> transactions;
    private List<FraudDetails> fraudDetails;
    private User user;
    private UserOrigin userOrigin;
    private UserState userState;
    private UserFacebookProfile userFacebookProfile;
    private List<Property> properties;

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public UserData() {
        statistics = Lists.newArrayList();
    }

    public void addStatistics(List<Statistic> statistics) {
        this.statistics.addAll(statistics);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<AccountBalance> getAccountBalanceHistory() {
        return accountBalanceHistory;
    }

    public List<CredentialsEvent> getCredentialsEvents() {
        return credentialsEvents;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }
    
    public List<FollowItem> getFollowItems() {
        return followItems;
    }

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public User getUser() {
        return user;
    }

    public UserOrigin getUserOrigin() {
        return userOrigin;
    }
    
    public UserState getUserState() {
        return userState;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setAccountBalanceHistory(List<AccountBalance> accountBalanceHistory) {
        this.accountBalanceHistory = accountBalanceHistory;
    }

    public void setCredentialsEvents(List<CredentialsEvent> credentialsEvents) {
        this.credentialsEvents = credentialsEvents;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public void setFollowItems(List<FollowItem> items) {
        this.followItems = items;
    }
    
    public void setStatistics(List<Statistic> statistics) {
        this.statistics = statistics;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserOrigin(UserOrigin userOrigin) {
        this.userOrigin = userOrigin;
    }
    
    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public UserFacebookProfile getUserFacebookProfile() {
        return userFacebookProfile;
    }

    public void setUserFacebookProfile(UserFacebookProfile userFacebookProfile) {
        this.userFacebookProfile = userFacebookProfile;
    }

    public List<FraudDetails> getFraudDetails() {
        return fraudDetails;
    }

    public void setFraudDetails(List<FraudDetails> fraudDetails) {
        this.fraudDetails = fraudDetails;
    }

    public ImmutableListMultimap<String, Loan> getLoanDataByAccount() {
        return loanDataByAccount;
    }

    public void setLoanDataByAccount(ImmutableListMultimap<String, Loan> loanDataByAccount) {
        this.loanDataByAccount = loanDataByAccount;
    }
}

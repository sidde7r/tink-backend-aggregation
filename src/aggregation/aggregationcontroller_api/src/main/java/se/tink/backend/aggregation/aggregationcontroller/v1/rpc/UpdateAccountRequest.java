package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.libraries.account.rpc.Account;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAccountRequest {
    private Account account;
    private AccountFeatures accountFeatures;
    private String user;
    private String credentialsId;
    private ExactCurrencyAmount availableBalance;
    private ExactCurrencyAmount creditLimit;

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public Account getAccount() {
        return account;
    }

    public String getUser() {
        return user;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public AccountFeatures getAccountFeatures() {
        if (accountFeatures == null) {
            return AccountFeatures.createEmpty();
        }
        return accountFeatures;
    }

    public void setAccountFeatures(AccountFeatures accountFeatures) {
        this.accountFeatures = accountFeatures;
    }

    public ExactCurrencyAmount getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(ExactCurrencyAmount availableBalance) {
        this.availableBalance = availableBalance;
    }

    public ExactCurrencyAmount getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(ExactCurrencyAmount creditLimit) {
        this.creditLimit = creditLimit;
    }
}

package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private Map<String, Object> accountId = new HashMap<String, Object>();
    private Map<String, Object> accountNumber = new HashMap<String, Object>();
    private Map<String, Object> accountType = new HashMap<String, Object>();
    private Map<String, Object> productTypeExtension = new HashMap<String, Object>();
    private Map<String, Object> currency = new HashMap<String, Object>();
    private Map<String, Object> nickName = new HashMap<String, Object>();
    private Map<String, Object> balance = new HashMap<String, Object>();
    private Map<String, Object> fundsAvailable = new HashMap<String, Object>();
    private Map<String, Object> branchId = new HashMap<String, Object>();

    public Map<String, Object> getAccountId() {
        return accountId;
    }

    public void setAccountId(Map<String, Object> accountId) {
        this.accountId = accountId;
    }

    public Map<String, Object> getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Map<String, Object> accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Map<String, Object> getAccountType() {
        return accountType;
    }

    public void setAccountType(Map<String, Object> accountType) {
        this.accountType = accountType;
    }

    public Map<String, Object> getProductTypeExtension() {
        return productTypeExtension;
    }

    public void setProductTypeExtension(Map<String, Object> productTypeExtension) {
        this.productTypeExtension = productTypeExtension;
    }

    public Map<String, Object> getCurrency() {
        return currency;
    }

    public void setCurrency(Map<String, Object> currency) {
        this.currency = currency;
    }

    public Map<String, Object> getNickName() {
        return nickName;
    }

    public void setNickName(Map<String, Object> nickName) {
        this.nickName = nickName;
    }

    public Map<String, Object> getBalance() {
        return balance;
    }

    public void setBalance(Map<String, Object> balance) {
        this.balance = balance;
    }

    public Map<String, Object> getFundsAvailable() {
        return fundsAvailable;
    }

    public void setFundsAvailable(Map<String, Object> fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public Map<String, Object> getBranchId() {
        return branchId;
    }

    public void setBranchId(Map<String, Object> branchId) {
        this.branchId = branchId;
    }
}

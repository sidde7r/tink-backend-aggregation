package se.tink.backend.aggregation.agents.banks.nordea.v18.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEntity {
    private Map<String, Object> accountType = new HashMap<String, Object>();
    private Map<String, Object> balance = new HashMap<String, Object>();
    private Map<String, Object> branchId = new HashMap<String, Object>();
    private Map<String, Object> cardGroup = new HashMap<String, Object>();
    private Map<String, Object> currency = new HashMap<String, Object>();
    private Map<String, Object> fundsAvailable = new HashMap<String, Object>();
    private Map<String, Object> isMainCard = new HashMap<String, Object>();
    private Map<String, Object> nickName = new HashMap<String, Object>();
    private Map<String, Object> productCode = new HashMap<String, Object>();
    private Map<String, Object> productId = new HashMap<String, Object>();
    private Map<String, Object> productNumber = new HashMap<String, Object>();
    private Map<String, Object> productType = new HashMap<String, Object>();
    private Map<String, Object> productTypeExtension = new HashMap<String, Object>();
    private Map<String, Object> warningCode = new HashMap<String, Object>();

    public Map<String, Object> getAccountType() {
        return accountType;
    }

    public Map<String, Object> getBalance() {
        return balance;
    }

    public Map<String, Object> getBranchId() {
        return branchId;
    }

    public Map<String, Object> getCardGroup() {
        return cardGroup;
    }

    public Map<String, Object> getCurrency() {
        return currency;
    }

    public Map<String, Object> getFundsAvailable() {
        return fundsAvailable;
    }

    public Map<String, Object> getIsMainCard() {
        return isMainCard;
    }

    public Map<String, Object> getNickName() {
        return nickName;
    }

    public Map<String, Object> getProductCode() {
        return productCode;
    }

    public Map<String, Object> getProductId() {
        return productId;
    }

    public Map<String, Object> getProductNumber() {
        return productNumber;
    }

    public Map<String, Object> getProductType() {
        return productType;
    }

    public Map<String, Object> getProductTypeExtension() {
        return productTypeExtension;
    }

    public Map<String, Object> getWarningCode() {
        return warningCode;
    }

    public void setAccountType(Map<String, Object> accountType) {
        this.accountType = accountType;
    }

    public void setBalance(Map<String, Object> balance) {
        this.balance = balance;
    }

    public void setBranchId(Map<String, Object> branchId) {
        this.branchId = branchId;
    }

    public void setCardGroup(Map<String, Object> cardGroup) {
        this.cardGroup = cardGroup;
    }

    public void setCurrency(Map<String, Object> currency) {
        this.currency = currency;
    }

    public void setFundsAvailable(Map<String, Object> fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public void setIsMainCard(Map<String, Object> isMainCard) {
        this.isMainCard = isMainCard;
    }

    public void setNickName(Map<String, Object> nickName) {
        this.nickName = nickName;
    }

    public void setProductCode(Map<String, Object> productCode) {
        this.productCode = productCode;
    }

    public void setProductId(Map<String, Object> productId) {
        this.productId = productId;
    }

    public void setProductNumber(Map<String, Object> productNumber) {
        this.productNumber = productNumber;
    }

    public void setProductType(Map<String, Object> productType) {
        this.productType = productType;
    }

    public void setProductTypeExtension(Map<String, Object> productTypeExtension) {
        this.productTypeExtension = productTypeExtension;
    }

    public void setWarningCode(Map<String, Object> warningCode) {
        this.warningCode = warningCode;
    }
}

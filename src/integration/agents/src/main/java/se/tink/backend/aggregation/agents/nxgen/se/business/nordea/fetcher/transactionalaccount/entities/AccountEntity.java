package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String accountType;
    private String balance;
    private String balanceForeignCurrency;
    private String cardGroup;
    private String currency;
    private String fundsAvailable;
    private String nickName;
    private AccountIdEntity productId;
    private String productNumber;
    private String productType;
    private String productTypeExtension;

    public String getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getProductTypeExtension() {
        return productTypeExtension;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getFundsAvailable() {
        return fundsAvailable;
    }

    public AccountIdEntity getProductId() {
        return productId;
    }

    public String getNickName() {
        return nickName;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getBalanceForeignCurrency() {
        return balanceForeignCurrency;
    }

    public String getCardGroup() {
        return cardGroup;
    }

    public String getProductType() {
        return productType;
    }
}

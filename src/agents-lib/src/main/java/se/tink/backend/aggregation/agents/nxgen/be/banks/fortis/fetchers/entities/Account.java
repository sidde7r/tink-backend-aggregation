package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Account {
    private String relationType;
    private ConvertedBalance convertedBalance;
    private String productId;
    private String accountName;
    private String snum;
    private String accountType;
    private FlagsDTO flagsDTO;
    private String accountFamily;
    private String accountRelationship;
    private String refusalDate;
    private String contractNumber;
    private String accountTypeFullName;
    private Balance balance;
    private String iban;
    private String alias;
    private String currency;
    private String accountSubFamily;

    public String getRelationType() {
        return relationType;
    }

    public ConvertedBalance getConvertedBalance() {
        return convertedBalance;
    }

    public String getProductId() {
        return productId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getSnum() {
        return snum;
    }

    public String getAccountType() {
        return accountType;
    }

    public FlagsDTO getFlagsDTO() {
        return flagsDTO;
    }

    public String getAccountFamily() {
        return accountFamily;
    }

    public String getAccountRelationship() {
        return accountRelationship;
    }

    public String getRefusalDate() {
        return refusalDate;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getAccountTypeFullName() {
        return accountTypeFullName;
    }

    public Balance getBalance() {
        return balance;
    }

    public String getIban() {
        return iban;
    }

    public String getAlias() {
        return alias;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountSubFamily() {
        return accountSubFamily;
    }
}

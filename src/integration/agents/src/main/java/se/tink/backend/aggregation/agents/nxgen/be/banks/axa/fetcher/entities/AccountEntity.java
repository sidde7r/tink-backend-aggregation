package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AccountEntity {
    private String accountNumber;
    private String accountType;
    private String adminPointOfSale;
    private String availableAmount;
    private String balance;
    private String creditLineAmount;
    private String currency;
    private String displayName;
    private Boolean hasAutomaticPayments;
    private Boolean hasDefinitRefusals;
    private Boolean hasTempRefusals;
    private Boolean isPartialAccount;
    private Boolean isStartAccount;
    private String productRoleType;
    private String sortAccount;
    private String titularName;
    private String typeDescription; // Language-dependent

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public String getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitularName() {
        return titularName;
    }

    public String getTypeDescription() {
        return typeDescription;
    }
}

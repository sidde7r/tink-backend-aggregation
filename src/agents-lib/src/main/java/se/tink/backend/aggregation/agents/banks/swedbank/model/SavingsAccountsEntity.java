package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingsAccountsEntity {
    private SavingsAccountDetailsEntity details;
    private String name;
    private String id;
    private AmountEntity balance;
    private String accountNumber;
    private LinksEntity links;

    public SavingsAccountDetailsEntity getDetails() {
        return details;
    }

    public void setDetails(SavingsAccountDetailsEntity details) {
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public void setBalance(AmountEntity balance) {
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}

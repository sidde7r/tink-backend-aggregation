package se.tink.backend.rpc;

import se.tink.backend.core.AccountTypes;

public class UpdateAccountRequest {
    private String accountNumber;
    private String name;
    private AccountTypes type;
    private Boolean favored;
    private Boolean excluded;
    private Double ownership;

    public UpdateAccountRequest() {
    }

    public UpdateAccountRequest(String accountNumber, String name, AccountTypes type, Boolean favored, Boolean excluded,
            Double ownership) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.type = type;
        this.favored = favored;
        this.excluded = excluded;
        this.ownership = ownership;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountTypes getType() {
        return type;
    }

    public void setType(AccountTypes type) {
        this.type = type;
    }

    public Boolean getFavored() {
        return favored;
    }

    public void setFavored(Boolean favored) {
        this.favored = favored;
    }

    public Boolean getExcluded() {
        return excluded;
    }

    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    public Double getOwnership() {
        return ownership;
    }

    public void setOwnership(Double ownership) {
        this.ownership = ownership;
    }
}

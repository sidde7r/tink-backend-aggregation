package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String bicFi;
    private AccountIdEntity accountId;
    private String name;
    private String details;
    private String linkedAccount;
    private String usage;
    private String cashAccountType;
    private String product;

    public String getResourceId() {
        return resourceId;
    }

    public String getBicFi() {
        return bicFi;
    }

    public AccountIdEntity getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public String getLinkedAccount() {
        return linkedAccount;
    }

    public String getUsage() {
        return usage;
    }

    public String getCashAccountType() {
        return cashAccountType;
    }

    public String getProduct() {
        return product;
    }
}

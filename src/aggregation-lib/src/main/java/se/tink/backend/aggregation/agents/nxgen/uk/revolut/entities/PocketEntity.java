package se.tink.backend.aggregation.agents.nxgen.uk.revolut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class PocketEntity {
    private String id;
    private String type;
    private String state;
    private String currency;
    private int balance;
    private int blockedAmount;
    private boolean closed;
    private int creditLimit;
    private String name;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(AccountEntity accountEntity) {
        return null;
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount(List<AccountEntity> accountEntities) {
        return null;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getState() {
        return state;
    }

    public String getCurrency() {
        return currency;
    }

    public int getBalance() {
        return balance;
    }

    public int getBlockedAmount() {
        return blockedAmount;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getCreditLimit() {
        return creditLimit;
    }

    public String getName() {
        return name;
    }
}

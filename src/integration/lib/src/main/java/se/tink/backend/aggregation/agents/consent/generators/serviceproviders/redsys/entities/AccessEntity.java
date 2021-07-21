package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonInclude(Include.NON_NULL)
public class AccessEntity {

    private static final String ALL_ACCOUNTS = "allAccounts";

    @JsonProperty private List<AccountInfoEntity> accounts = null;

    @JsonProperty private List<AccountInfoEntity> balances = null;

    @JsonProperty private List<AccountInfoEntity> transactions = null;

    @JsonProperty private String availableAccounts = null;

    @JsonProperty private String availableAccountsWithBalances = null;

    @JsonProperty private String allPsd2 = null;

    public AccessEntity setAccounts(List<AccountInfoEntity> accounts) {
        this.accounts = accounts;
        return this;
    }

    public AccessEntity setBalances(List<AccountInfoEntity> balances) {
        this.balances = balances;
        return this;
    }

    public AccessEntity setTransactions(List<AccountInfoEntity> transactions) {
        this.transactions = transactions;
        return this;
    }

    public AccessEntity setAvailableAccounts(String availableAccounts) {
        this.availableAccounts = availableAccounts;
        return this;
    }

    public AccessEntity setAvailableAccountsWithBalances(String availableAccountsWithBalances) {
        this.availableAccountsWithBalances = availableAccountsWithBalances;
        return this;
    }

    public AccessEntity setAllPsd2(String allPsd2) {
        this.allPsd2 = allPsd2;
        return this;
    }

    public static AccessEntity ofAllAccounts() {
        return new AccessEntity().setAvailableAccounts(ALL_ACCOUNTS);
    }

    public static AccessEntity ofAllAccountsWithBalances() {
        return new AccessEntity().setAvailableAccountsWithBalances(ALL_ACCOUNTS);
    }

    public static AccessEntity ofAllPsd2() {
        return new AccessEntity().setAllPsd2(ALL_ACCOUNTS);
    }

    public static AccessEntity ofAccountWithBalancesAndTransactions(String iban) {
        List<AccountInfoEntity> accountInfoEntities =
                Collections.singletonList(new AccountInfoEntity(iban));
        return new AccessEntity()
                .setAccounts(accountInfoEntities)
                .setBalances(accountInfoEntities)
                .setTransactions(accountInfoEntities);
    }
}

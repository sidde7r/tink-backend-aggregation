package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("accountHolderName")
    private String accountHolderName;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    @JsonIgnore private AccountEntity accounts;

    public AccountEntity getAccounts() {
        return new AccountEntity(accountNumber, currency, accountHolderName);
    }

    public Stream<AccountEntity> stream() {
        List<AccountEntity> accounts = new ArrayList<>();
        accounts.add(getAccounts());
        return accounts.stream();
    }
}

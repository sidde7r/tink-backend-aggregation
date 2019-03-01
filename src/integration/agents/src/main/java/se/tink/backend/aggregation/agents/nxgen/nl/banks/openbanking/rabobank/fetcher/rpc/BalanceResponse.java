package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

import java.util.List;
import java.util.Objects;

@JsonObject
public class BalanceResponse {

    @JsonProperty("balances")
    private List<BalancesItem> balances;

    @JsonProperty("account")
    private Account account;

    public void setBalances(List<BalancesItem> balances) {
        this.balances = balances;
    }

    public List<BalancesItem> getBalances() {
        return balances;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "BalanceResponse{"
                + "balances = '"
                + balances
                + '\''
                + ",account = '"
                + account
                + '\''
                + "}";
    }

    @JsonIgnore
    public Amount toAmount() {
        if (Objects.isNull(balances) || balances.isEmpty()) {
            throw new IllegalStateException("No balances for account!");
        }
        BalancesItem balance = balances.get(0);
        return new Amount(
                balance.getBalanceAmount().getCurrency(), balance.getBalanceAmount().getAmount());
    }
}

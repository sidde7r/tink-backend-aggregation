package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceResponse {

    @JsonProperty("balances")
    private List<BalancesItem> balances;

    @JsonProperty("account")
    private Account account;

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
    public ExactCurrencyAmount toAmount() {
        // TODO fix this
        BalancesItem balance = balances.get(0);
        return ExactCurrencyAmount.of(
                balance.getBalanceAmount().getAmount(), balance.getBalanceAmount().getCurrency());
    }
}

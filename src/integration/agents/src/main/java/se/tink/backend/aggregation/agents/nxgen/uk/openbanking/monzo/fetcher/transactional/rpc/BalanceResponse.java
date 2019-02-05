package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

/**
 * Monzo has something called "pots" - some sort of subaccounts. The balance of the pots is not included in "balance",
 * but it is included in "total_balance". Either you use balance and get the pots/subaccounts separately, or you use
 * totalBalance and disregard the pots. Since Tink doesn't have the concept of subaccounts, we use totalBalance.
 */
@JsonObject
public class BalanceResponse {

    private int balance;
    @JsonProperty("total_balance")
    private int totalBalance;
    private String currency;
    @JsonProperty("spend_today")
    private int spendToday;
    @JsonProperty("local_currency")
    private String localCurrency;
    @JsonProperty("local_exchange_rate")
    private int localExchangeRate;
    @JsonProperty("local_spend")
    private List<Object> localSpend;
    @JsonProperty("overdraft_limit")
    private int overdraftLimit;

    public Amount getBalance() {
        return Amount.valueOf(currency, balance, 2);
    }

    public Amount getTotalBalance() {
        return Amount.valueOf(currency, totalBalance, 2);
    }

}

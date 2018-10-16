package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class BalanceEntity {

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

    Amount getBalance() {
        return Amount.valueOf(currency, balance, 2);
    }

}

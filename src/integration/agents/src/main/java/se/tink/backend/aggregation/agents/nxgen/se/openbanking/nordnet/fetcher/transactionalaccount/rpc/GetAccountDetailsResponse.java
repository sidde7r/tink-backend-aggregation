package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountDetailsResponse {
    @JsonProperty("account_credit")
    private AccountDetailsEntity accountCredit;

    private AccountDetailsEntity collateral;

    @JsonProperty("pawn_value")
    private AccountDetailsEntity pawnValue;

    @JsonProperty("trading_power")
    private AccountDetailsEntity tradingPower;

    @JsonProperty("loan_limit")
    private AccountDetailsEntity loanLimit;

    @JsonProperty("forward_sum")
    private AccountDetailsEntity forwardSum;

    @JsonProperty("future_sum")
    private AccountDetailsEntity futureSum;

    @JsonProperty("account_currency")
    private String accountCurrency;

    private AccountDetailsEntity interest;

    @JsonProperty("account_sum")
    private AccountDetailsEntity accountSum;

    @JsonProperty("unrealized_future_profit_loss")
    private AccountDetailsEntity unrealizedFutureProfitLoss;

    @JsonProperty("own_capital")
    private AccountDetailsEntity ownCapital;

    @JsonProperty("own_capital_morning")
    private AccountDetailsEntity ownCapitalMorning;

    @JsonProperty("full_marketvalue")
    private AccountDetailsEntity fullMarketvalue;
}

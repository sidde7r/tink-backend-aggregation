package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountInfoEntity {

    @JsonProperty("account_credit")
    private AmountEntity accountCredit;

    @Getter private AmountEntity collateral;

    @Getter
    @JsonProperty("pawn_value")
    private AmountEntity pawnValue;

    @Getter
    @JsonProperty("trading_power")
    private AmountEntity tradingPower;

    @JsonProperty("loan_limit")
    private AmountEntity loanLimit;

    @JsonProperty("forward_sum")
    private AmountEntity forwardSum;

    @JsonProperty("future_sum")
    private AmountEntity futureSum;

    @JsonProperty("account_currency")
    private String accountCurrency;

    @JsonProperty("account_sum")
    private AmountEntity accountSum;

    @JsonProperty("unrealized_future_profit_loss")
    private AmountEntity unrealizedFutureProfitLoss;

    @JsonProperty("own_capital")
    private AmountEntity ownCapital;

    @JsonProperty("own_capital_morning")
    private AmountEntity ownCapitalMorning;

    @JsonProperty("full_marketvalue")
    private AmountEntity fullMarketvalue;

    @JsonProperty("account_code")
    private String accountCode;

    @JsonProperty("bank_accno")
    private String bankAccountNumber;

    @JsonProperty("bank_account")
    private BankAccountEntity bankAccount;

    @JsonProperty("interest_rate")
    private double interestRate;

    @JsonProperty("registration_date")
    private String registrationDate;

    @JsonProperty("accno")
    private String accountNumber;

    @JsonProperty("accid")
    private String accountId;

    @JsonIgnore
    public ExactCurrencyAmount getAccountSum() {
        return new ExactCurrencyAmount(accountSum.getValue(), accountSum.getCurrency());
    }

    @JsonIgnore
    public ExactCurrencyAmount getFullMarketValue() {
        return new ExactCurrencyAmount(fullMarketvalue.getValue(), fullMarketvalue.getCurrency());
    }
}

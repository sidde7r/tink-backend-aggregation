package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {

    @JsonProperty("account_credit")
    private AmountEntity accountCredit;
    private AmountEntity collateral;
    @JsonProperty("pawn_value")
    private AmountEntity pawnValue;
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
    private AmountEntity interest;
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
    private String bankAccno;
    @JsonProperty("bank_account")
    private BankAccountEntity bankAccount;
    @JsonProperty("interest_rate")
    private double interestRate;
    @JsonProperty("registration_date")
    private String registrationDate;
    private int accno;
    private int accid;

    public int getAccid() {
        return accid;
    }

    public AmountEntity getAccountSum() {
        return accountSum;
    }

    public AmountEntity getFullMarketvalue() {
        return fullMarketvalue;
    }

}

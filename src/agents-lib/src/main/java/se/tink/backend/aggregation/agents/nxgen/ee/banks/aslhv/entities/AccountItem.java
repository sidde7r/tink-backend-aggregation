package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountItem {

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("type")
    private int type;

    @JsonProperty("number")
    private String number;

    @JsonProperty("balance")
    private List<Balance> balances;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("name")
    private String name;

    @JsonProperty("portfolio_id")
    private String portfolioId;

    @JsonProperty("free_amount")
    private double freeAmount;

    @JsonProperty("free_credit_amount")
    private double freeCreditAmount;

    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public AccountTypes getType() {
        return AsLhvConstants.ACCOUNT_TYPE_MAPPER.translate(type)
                .orElseThrow(() -> new IllegalStateException(
                        "Unknown account types should have been filtered out before reaching this point!"));
    }

    @JsonIgnore
    public Optional<String> getNumber() {
        return Optional.ofNullable(number);
    }

    @JsonIgnore
    public double getBalance(int baseCurrencyId) {
        double result = freeAmount;
        if (balances != null) {
            for (Balance balance : balances) {
                if (balance.getCurrencyId() == baseCurrencyId) {
                    result =  balance.getFreeAmount();
                }
            }
        }
        return result;
    }

    @JsonIgnore
    public double getFreeCredit(int baseCurrencyId) {
        double result = freeCreditAmount;
        if (balances != null) {
            for (Balance balance : balances) {
                if (balance.getCurrencyId() == baseCurrencyId) {
                    result =  balance.getFreeCreditAmount();
                }
            }
        }
        return result;
    }

    @JsonIgnore
    public Optional<String> getIban() {
        return Optional.ofNullable(iban);
    }

    @JsonIgnore
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @JsonIgnore
    public Optional<String> getPortfolioId() {
        return Optional.ofNullable(portfolioId);
    }
}

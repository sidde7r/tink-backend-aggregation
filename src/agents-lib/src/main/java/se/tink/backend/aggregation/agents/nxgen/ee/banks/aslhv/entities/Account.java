package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class Account {

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("type")
    private int type;

    @JsonProperty("free_credit_amount")
    private double freeCreditAmount;

    @JsonProperty("pending_payments")
    private PendingPayments pendingPayments;

    @JsonProperty("securities")
    private Securities securities;

    @JsonProperty("number")
    private long number;

    @JsonProperty("balance")
    private List<Balance> balances;

    @JsonProperty("reserved_amount")
    private double reservedAmount;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("name")
    private String name;

    @JsonProperty("portfolio_id")
    private String portfolioId;

    @JsonProperty("einvoices")
    private Einvoices einvoices;

    @JsonProperty("actions")
    private List<Action> actions;

    @JsonProperty("free_amount")
    private double freeAmount;

    public boolean isActive() {
        return active;
    }

    public AccountTypes getType() {
        return AsLhvConstants.ACCOUNT_TYPE_MAPPER.translate(type)
                .orElseThrow(() -> new IllegalStateException(
                        "Unknown account types should have been filtered out before reaching this point!"));
    }

    public double getFreeCreditAmount() {
        return freeCreditAmount;
    }

    public PendingPayments getPendingPayments() {
        return pendingPayments;
    }

    public Securities getSecurities() {
        return securities;
    }

    public long getNumber() {
        return number;
    }

    public double getBalance(int baseCurrencyId) {
        for (Balance balance : balances) {
            if (balance.getCurrencyId() == baseCurrencyId) {
                return balance.getFreeAmount();
            }
        }
        return 0;
    }

    public double getReservedAmount() {
        return reservedAmount;
    }

    public String getIban() {
        return iban;
    }

    public String getName() {
        return name;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public Einvoices getEinvoices() {
        return einvoices;
    }

    public List<Action> getActions() {
        return actions;
    }

    public double getFreeAmount() {
        return freeAmount;
    }
}

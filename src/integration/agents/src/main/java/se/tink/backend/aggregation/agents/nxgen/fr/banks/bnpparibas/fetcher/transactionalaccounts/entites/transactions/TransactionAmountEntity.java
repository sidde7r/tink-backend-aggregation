package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionAmountEntity {
    private String currency;
    private String executionDate;

    @JsonDouble
    @JsonProperty("montant")
    private double amount;

    private String valueDate;

    @JsonIgnore
    public Amount getTinkAmount() {
        return new Amount(currency, amount);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }
}

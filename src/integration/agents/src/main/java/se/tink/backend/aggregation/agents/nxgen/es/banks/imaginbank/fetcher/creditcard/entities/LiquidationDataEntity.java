package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LiquidationDataEntity {

    @JsonProperty("saldoPrepago")
    private double prepaidAmount;

    @JsonProperty("saldoDisponible")
    private double availableCredit;

    @JsonProperty("saldoDispuesto")
    private double balance;

    @JsonProperty("gastoMesDebito")
    private String spendingMonthDebit;

    @JsonProperty("datosUltimoMovimiento")
    private LastMovementDataEntity lastMovementData;

    public ExactCurrencyAmount getPrepaidAmount() {
        return ExactCurrencyAmount.inEUR(prepaidAmount);
    }

    public ExactCurrencyAmount getAvailableCredit() {
        return ExactCurrencyAmount.inEUR(availableCredit);
    }

    public ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.inEUR(balance);
    }
}

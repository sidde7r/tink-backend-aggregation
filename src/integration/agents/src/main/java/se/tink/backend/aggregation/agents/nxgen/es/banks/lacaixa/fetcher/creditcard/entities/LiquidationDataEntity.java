package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

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

    public Amount getPrepaidAmount() {
        return Amount.inEUR(prepaidAmount);
    }

    public Amount getAvailableCredit() {
        return Amount.inEUR(availableCredit);
    }

    public Amount getBalance() {
        return Amount.inEUR(balance);
    }

}

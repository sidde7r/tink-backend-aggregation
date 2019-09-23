package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericLiquidationDataEntity {

    @JsonProperty("saldoPrepago")
    private BigDecimal prepaidAmount;

    @JsonProperty("saldoDisponible")
    private BigDecimal availableCredit;

    @JsonProperty("saldoDispuesto")
    private BigDecimal balance;

    @JsonProperty("gastoMesDebito")
    private String spendingMonthDebit;

    @JsonProperty("datosUltimoMovimiento")
    private LastMovementDataEntity lastMovementData;

    public BigDecimal getPrepaidAmount() {
        return prepaidAmount;
    }

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}

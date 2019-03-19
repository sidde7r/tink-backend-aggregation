package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanInstallmentsEntity {
    @JsonProperty("importeCuotaActual")
    private AmountEntity currentAmortizationAmount;
    private QuantityEntity plazoPendienteUltimaFacturacion;
    private int numeroCuotasPrestamo;
    @JsonProperty("importeProximaCuota")
    private AmountEntity nextAmortizationAmount;

    public AmountEntity getCurrentAmortizationAmount() {
        return currentAmortizationAmount;
    }

    public AmountEntity getNextAmortizationAmount() {
        return nextAmortizationAmount;
    }
}

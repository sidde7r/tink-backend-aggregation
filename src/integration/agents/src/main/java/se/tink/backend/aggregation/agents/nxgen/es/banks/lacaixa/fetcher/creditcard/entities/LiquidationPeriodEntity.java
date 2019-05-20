package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LiquidationPeriodEntity {
    @JsonProperty("fechaInicio")
    private String startDate;

    @JsonProperty("fechaFin")
    private String endDate;

    @JsonProperty("operaciones")
    private double operations;

    @JsonProperty("bonificaciones")
    private double bonuses;

    @JsonProperty("interesAplazado")
    private double interestPostponed;

    @JsonProperty("interesOperaciones")
    private double interestOperations;

    @JsonProperty("precioServicio")
    private double servicePrice;

    @JsonProperty("liquidacionActual")
    private double currentSettlement;

    @JsonProperty("liquidacionAnterior")
    private double liquidationPrevious;

    @JsonProperty("miDeuda")
    private double myDebt;

    @JsonProperty("deudaPendiente")
    private double pendingDebt;

    @JsonProperty("importeAPagar")
    private double amountToPay;

    public double getMyDebt() {
        return myDebt;
    }
}

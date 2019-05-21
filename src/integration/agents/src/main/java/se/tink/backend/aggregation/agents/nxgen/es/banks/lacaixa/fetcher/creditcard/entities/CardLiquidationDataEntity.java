package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.LiquidationSimulation;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardLiquidationDataEntity {
    @JsonProperty("fechaInicio")
    private DateEntity startDate;

    @JsonProperty("fechaFin")
    private DateEntity endDate;

    @JsonProperty("fechaCobro")
    private DateEntity collectionDate;

    @JsonProperty("signo")
    private String sign;

    @JsonProperty("importe")
    private double amount;

    @JsonProperty("moneda")
    private String currency;

    @JsonProperty("codSimulacion")
    private String simulationCode;

    @JsonProperty("indSimulacion")
    private String simulationDescription;

    public DateEntity getEndDate() {
        return endDate;
    }

    public boolean isSimulation() {
        return LiquidationSimulation.TRUE.equalsIgnoreCase(simulationCode);
    }
}

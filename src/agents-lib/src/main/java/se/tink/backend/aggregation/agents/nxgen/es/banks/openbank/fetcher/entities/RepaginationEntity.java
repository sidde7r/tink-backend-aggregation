package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RepaginationEntity {
    @JsonProperty("fechaAlta")
    private String fechaAlta;

    @JsonProperty("diaMovimiento")
    private int diaMovimiento;

    @JsonProperty("situacion")
    private String situacion;

    @JsonProperty("indicadorMas")
    private String indicadorMas;

    @JsonProperty("operacionDGO")
    private OperacionDGO operacionDGO;

    @JsonProperty("numeroMovimiento")
    private int numeroMovimiento;

    public String getFechaAlta() {
        return fechaAlta;
    }

    public int getDiaMovimiento() {
        return diaMovimiento;
    }

    public String getSituacion() {
        return situacion;
    }

    public String getIndicadorMas() {
        return indicadorMas;
    }

    public OperacionDGO getOperacionDGO() {
        return operacionDGO;
    }

    public int getNumeroMovimiento() {
        return numeroMovimiento;
    }
}

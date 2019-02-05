package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RepaginationEntity {
    @JsonProperty("fechaAlta")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fechaAlta;

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

    public Date getFechaAlta() {
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

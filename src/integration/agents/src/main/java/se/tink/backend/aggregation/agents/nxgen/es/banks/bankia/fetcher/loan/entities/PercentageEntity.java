package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PercentageEntity {
    @JsonProperty("porcentaje")
    private int percentage;

    @JsonProperty("numDecimales")
    private String numDecimals;

    @JsonProperty("unidadMedidaPorcentual")
    private String unitMeasurePercentual;

    @JsonIgnore
    public BigDecimal percentageValue() {
        int scale = Strings.isNullOrEmpty(numDecimals) ? 0 : Integer.parseInt(numDecimals);
        return BigDecimal.valueOf(percentage, scale);
    }
}

package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailLineEntity {
    @JsonProperty("AM")
    private int balanceType;

    @JsonProperty("T")
    private int type;

    @JsonProperty("L")
    private String label;

    @JsonProperty("V")
    private String value;

    @JsonProperty("DV")
    private BigDecimal decimalValue;

    @JsonProperty("Linhas")
    private List<DetailLineEntity> lines;

    public Integer getBalanceType() {
        return balanceType;
    }

    public Integer getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public List<DetailLineEntity> getLines() {
        return lines;
    }
}

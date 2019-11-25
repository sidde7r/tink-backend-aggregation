package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SectionEntity {
    @JsonProperty("Designacao")
    private String designation;

    @JsonProperty("Valor")
    private BigDecimal value;

    @JsonProperty("Estado")
    private int state;

    @JsonProperty("Tipo")
    private int type;

    @JsonProperty("Detalhes")
    private List<DetailsEntity> details;

    public String getDesignation() {
        return designation;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getState() {
        return state;
    }

    public int getType() {
        return type;
    }

    public List<DetailsEntity> getDetails() {
        return details;
    }
}

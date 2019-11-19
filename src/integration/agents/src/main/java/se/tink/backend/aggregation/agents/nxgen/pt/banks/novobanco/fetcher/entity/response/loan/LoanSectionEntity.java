package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanSectionEntity {
    @JsonProperty("Designacao")
    private String designation;

    @JsonProperty("Valor")
    private double value;

    @JsonProperty("Estado")
    private int state;

    @JsonProperty("Tipo")
    private int type;

    @JsonProperty("Detalhes")
    private List<LoanOverviewEntity> details;

    public String getDesignation() {
        return designation;
    }

    public double getValue() {
        return value;
    }

    public int getState() {
        return state;
    }

    public int getType() {
        return type;
    }

    public List<LoanOverviewEntity> getLoansOverview() {
        return details;
    }
}

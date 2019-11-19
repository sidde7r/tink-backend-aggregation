package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanLinesEntity {
    @JsonProperty("AM")
    private int am;

    @JsonProperty("T")
    private int t;

    @JsonProperty("L")
    private String l;

    @JsonProperty("V")
    private String v;

    @JsonProperty("DV")
    private double dv;

    @JsonProperty("Linhas")
    private List<LoanLinesEntity> lines;

    public int getAm() {
        return am;
    }

    public int getT() {
        return t;
    }

    public String getL() {
        return l;
    }

    public String getV() {
        return v;
    }

    public double getDv() {
        return dv;
    }

    public List<LoanLinesEntity> getLines() {
        return lines;
    }
}

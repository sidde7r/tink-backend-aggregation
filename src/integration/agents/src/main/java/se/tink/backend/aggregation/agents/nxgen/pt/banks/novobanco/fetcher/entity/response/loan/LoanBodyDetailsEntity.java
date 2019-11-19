package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanBodyDetailsEntity {
    @JsonProperty("Header")
    private LoanDetailsHeaderEntity header;

    @JsonProperty("Linhas")
    private List<LoanLinesEntity> lines;

    public LoanDetailsHeaderEntity getHeader() {
        return header;
    }

    public List<LoanLinesEntity> getLines() {
        return lines;
    }
}

package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.DetailLineEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanBodyDetailsEntity {
    @JsonProperty("Header")
    private LoanDetailsHeaderEntity header;

    @JsonProperty("Linhas")
    private List<DetailLineEntity> lines;

    public LoanDetailsHeaderEntity getHeader() {
        return header;
    }

    public List<DetailLineEntity> getLines() {
        return lines;
    }
}

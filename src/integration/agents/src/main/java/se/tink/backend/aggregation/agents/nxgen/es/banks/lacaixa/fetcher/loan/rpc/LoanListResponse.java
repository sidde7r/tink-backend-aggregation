package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanListResponse {
    @JsonProperty("prestamos")
    private List<LoanEntity> loans;

    @JsonProperty("masDatos")
    private Boolean moreData;

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public Boolean getMoreData() {
        return moreData;
    }
}

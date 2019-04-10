package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanListResponse {
    private List<LoanEntity> loans;

    @JsonProperty("_aggregatedAmountFraction")
    private String aggregatedAmountFraction;

    @JsonProperty("_aggregatedAmountInteger")
    private String aggregatedAmountInteger;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public List<LoanEntity> getLoans() {
        return loans == null ? Collections.emptyList() : loans;
    }

    public String getAggregatedAmountFraction() {
        return aggregatedAmountFraction;
    }

    public String getAggregatedAmountInteger() {
        return aggregatedAmountInteger;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}

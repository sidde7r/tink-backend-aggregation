package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanSubTitleEntity {
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
}

package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PCBW431Z {
    @JsonProperty("ROW_ID")
    public int ROW_ID = 0;

    @JsonProperty("KONTO_NR")
    public String KONTO_NR;

    @JsonProperty("SEB_KUND_NR")
    public String SEB_KUND_NR;
}

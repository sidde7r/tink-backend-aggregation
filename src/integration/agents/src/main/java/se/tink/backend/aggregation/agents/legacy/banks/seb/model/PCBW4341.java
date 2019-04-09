package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PCBW4341 {
    @JsonProperty("ROW_ID")
    public int ROW_ID = 0;

    @JsonProperty("SEB_KUND_NR")
    public String SEB_KUND_NR;

    @JsonProperty("BELOPP_FROM")
    public String BELOPP_FROM = "0";

    @JsonProperty("BELOPP_TOM")
    public String BELOPP_TOM = "0";

    @JsonProperty("KONTO_NR")
    public String KONTO_NR;

    // 20 and 50 are used in the app
    @JsonProperty("MAX_ROWS")
    public int MAX_ROWS = 20;

    // Should be the last TRANSLOPNR in the previous batch
    @JsonProperty("TRANSLOPNR")
    public Integer TRANSLOPNR = 0;

    // Should be the last PCB_BOKF_DATUM in the previous batch, only used when fetching more
    @JsonProperty("PCB_BOKFDAT_BLADDR")
    public String PCB_BOKFDAT_BLADDR;
}

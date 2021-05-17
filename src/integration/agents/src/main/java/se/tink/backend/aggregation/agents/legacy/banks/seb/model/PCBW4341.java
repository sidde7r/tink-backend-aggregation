package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PCBW4341 {
    @JsonProperty("ROW_ID")
    private int rowId = 0;

    @Setter
    @JsonProperty("SEB_KUND_NR")
    private String customerId;

    @JsonProperty("BELOPP_FROM")
    private String beloppFrom = "0";

    @JsonProperty("BELOPP_TOM")
    private String beloppTom = "0";

    @Setter
    @JsonProperty("KONTO_NR")
    private String accountNumber;

    // 20 and 50 are used in the app
    @JsonProperty("MAX_ROWS")
    @Getter
    @Setter
    private int maxRows = 20;

    // Should be the last TRANSLOPNR in the previous batch
    @JsonProperty("TRANSLOPNR")
    @Setter
    private Integer translopnr = 0;

    // Should be the last PCB_BOKF_DATUM in the previous batch, only used when fetching more
    @JsonProperty("PCB_BOKFDAT_BLADDR")
    @Setter
    private String pcbBokfdatBladdr;
}

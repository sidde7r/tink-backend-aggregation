package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public class PCBW431Z {
    @JsonProperty("ROW_ID")
    private int rowId = 0;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("SEB_KUND_NR")
    private String customerId;
}

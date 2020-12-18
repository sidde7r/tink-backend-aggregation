package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class DebtDetailEntity {

    @JsonProperty("handelse")
    private String event;

    @JsonProperty("handelsedatum")
    private long eventDate;

    @JsonProperty("summa")
    private BigDecimal amount;

    @JsonIgnore
    public boolean isIncomingDebt() {
        return "Ingående skuld".equalsIgnoreCase(event);
    }

    @JsonIgnore
    public boolean isOutgoingDebt() {
        return "Utgående skuld".equalsIgnoreCase(event);
    }

    @JsonIgnore
    public boolean isInterest() {
        return "Beräknad ränta".equalsIgnoreCase(event);
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorStatusResponse {
    private String status;

    @JsonProperty("meddelande")
    private String message;

    @JsonIgnore
    public boolean isBankServiceClosed() {
        return VolvoFinansConstants.ErrorStatus.BANK_CLOSED.equalsIgnoreCase(status);
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignEInvoiceBody {
    @JsonProperty("RequestId")
    private String requestId;
    @JsonProperty("AutostartToken")
    private String autostartToken;

    public String getRequestId() {
        return requestId;
    }

    public String getAutostartToken() {
        return autostartToken;
    }
}

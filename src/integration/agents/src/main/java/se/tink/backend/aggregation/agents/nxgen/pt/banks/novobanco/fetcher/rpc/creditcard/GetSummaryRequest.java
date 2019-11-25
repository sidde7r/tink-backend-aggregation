package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetSummaryRequest {
    public GetSummaryRequest(HeaderEntity header) {
        this.header = header;
    }

    @JsonProperty("Header")
    private HeaderEntity header;
}

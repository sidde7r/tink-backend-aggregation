package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountDetailsRequest {

    @JsonProperty("accountNumber")
    private String handle;

    public FetchAccountDetailsRequest(final String handle) {
        this.handle = Objects.requireNonNull(handle);
    }
}

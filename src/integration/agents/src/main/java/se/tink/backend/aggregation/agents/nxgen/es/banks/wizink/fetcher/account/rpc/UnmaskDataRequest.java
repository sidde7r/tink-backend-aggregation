package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.UnmaskDataRequestBody;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnmaskDataRequest {

    @JsonProperty("UnmaskDataRequest")
    private UnmaskDataRequestBody requestBody;

    public UnmaskDataRequest(UnmaskDataRequestBody requestBody) {
        this.requestBody = requestBody;
    }
}

package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GlobalPositionRequest {
    @JsonProperty("GlobalPositionRequest")
    private GlobalPositionRequestBody requestBody;

    public GlobalPositionRequest() {
        this.requestBody = new GlobalPositionRequestBody();
    }

    @JsonObject
    private static class GlobalPositionRequestBody {
        boolean actualizeMutableData;

        private GlobalPositionRequestBody() {
            this.actualizeMutableData = true;
        }
    }
}

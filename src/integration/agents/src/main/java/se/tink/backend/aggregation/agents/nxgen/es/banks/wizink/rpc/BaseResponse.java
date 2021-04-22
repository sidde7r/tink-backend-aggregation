package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {
    private Result result;

    public Result getResult() {
        return result;
    }
}

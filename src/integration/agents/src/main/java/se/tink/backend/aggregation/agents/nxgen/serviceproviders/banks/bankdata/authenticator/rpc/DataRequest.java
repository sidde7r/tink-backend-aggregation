package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonRawValue;
import se.tink.backend.aggregation.annotations.JsonObject;

// TODO: Lots of requests only contain data, maybe refactor to something like this?
@JsonObject
public class DataRequest {
    @JsonRawValue private String data;

    public DataRequest(String data) {
        this.data = data;
    }
}

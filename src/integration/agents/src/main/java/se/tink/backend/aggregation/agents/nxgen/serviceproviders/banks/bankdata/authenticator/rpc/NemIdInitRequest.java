package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonRawValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdInitRequest {

    @JsonRawValue private String data;
    private String rsalabel;

    public NemIdInitRequest(String data, String rsalabel) {
        this.data = data;
        this.rsalabel = rsalabel;
    }
}

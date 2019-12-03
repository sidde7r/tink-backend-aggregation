package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonRawValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdEnrollmentRequest {
    @JsonRawValue private String data;

    public NemIdEnrollmentRequest(String data) {
        this.data = data;
    }
}

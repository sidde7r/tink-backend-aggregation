package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InfoResponse {

    private boolean requiresSmsVerification;

    @JsonIgnore
    public boolean requiresSmsVerification() {
        return requiresSmsVerification;
    }
}

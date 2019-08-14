package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.RequestBody;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdRequest {
    @JsonProperty("SEB_Referer")
    private String sebReferer;

    @JsonIgnore
    public BankIdRequest() {
        sebReferer = RequestBody.SEB_REFERER_VALUE;
    }
}

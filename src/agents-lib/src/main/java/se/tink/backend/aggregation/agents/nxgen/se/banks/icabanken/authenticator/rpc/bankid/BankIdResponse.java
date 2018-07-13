package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;


@JsonObject
public class BankIdResponse {

    @JsonProperty("Body")
    private SessionBodyEntity body;
    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public SessionBodyEntity getBody() {
        Preconditions.checkState(body != null, "Expected a body object but it was null");
        return body;
    }

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }
}

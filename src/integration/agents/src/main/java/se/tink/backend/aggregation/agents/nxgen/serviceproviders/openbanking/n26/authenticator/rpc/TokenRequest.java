package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.RequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class TokenRequest {

    private String id;
    private RequestPayload requestPayload;

    public TokenRequest() {}

    public TokenRequest(RequestPayload requestPayload) {
        this.requestPayload = requestPayload;
    }
}

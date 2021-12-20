package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorizationResponse {

    private String authorizationId;
    private String status;
    private String created;
    private String expires;
    private String modified;
    private String authorized;
    private String transactionTo;
    private String transactionFrom;
}

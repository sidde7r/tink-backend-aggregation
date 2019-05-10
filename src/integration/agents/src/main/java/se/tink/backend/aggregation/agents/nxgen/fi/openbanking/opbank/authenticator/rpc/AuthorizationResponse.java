package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationResponse {

    private String authorizationId;
    private String status;
    private String created;
    private String expires;
    private String modified;
    private String authorized;
    private String transactionTo;
    private String transactionFrom;

    public String getAuthorizationId() {
        return authorizationId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }

    public String getExpires() {
        return expires;
    }

    public String getModified() {
        return modified;
    }

    public String getAuthorized() {
        return authorized;
    }

    public String getTransactionTo() {
        return transactionTo;
    }

    public String getTransactionFrom() {
        return transactionFrom;
    }
}

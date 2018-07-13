package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionBodyEntity {
    @JsonProperty("RequestId")
    protected String requestId;
    @JsonProperty("Status")
    protected String status;
    @JsonProperty("SessionId")
    private String sessionId;
    @JsonProperty("AutostartToken")
    private String autostartToken;

    public String getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public BankIdStatus getBankIdStatus() {

        Preconditions.checkState(status != null, "BankID status was null");
        switch (status) {
            case "Ok":
                return BankIdStatus.DONE;
            case "Pending":
                return BankIdStatus.WAITING;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
            }
    }
}

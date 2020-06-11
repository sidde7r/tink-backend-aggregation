package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NoBankIdCollectResponse {
    private String code;
    private String token;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @JsonIgnore
    public BankIdStatus getBankIdStatus() {
        if ("COMPLETED".equals(code)) {
            return BankIdStatus.DONE;
        } else if ("WAITING".equals(code)) {
            return BankIdStatus.WAITING;
        } else {
            return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}

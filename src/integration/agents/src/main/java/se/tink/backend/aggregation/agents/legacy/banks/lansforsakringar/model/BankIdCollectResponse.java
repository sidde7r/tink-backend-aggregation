package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BankIdCollectResponse {
    private int expiresIn;
    private String oauthToken;
    private String resultCode;

    public BankIdStatus toBankIdStatus() {
        switch (resultCode.toLowerCase()) {
            case "outstanding_transaction":
            case "user_sign":
                return BankIdStatus.WAITING;
            case "complete":
                return BankIdStatus.DONE;
            case "usercancel":
                return BankIdStatus.CANCELLED;
            case "start_failed":
                return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
            case "no_client":
                return BankIdStatus.NO_CLIENT;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}

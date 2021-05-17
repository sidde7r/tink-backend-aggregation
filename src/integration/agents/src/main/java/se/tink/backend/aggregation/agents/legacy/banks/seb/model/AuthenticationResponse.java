package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponse {
    @JsonProperty("auto_start_token")
    private String autoStartToken;

    private String status;

    @JsonProperty("hint_code")
    private String hintCode;

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public String getHintCode() {
        return hintCode;
    }

    @JsonIgnore
    public BankIdStatus toBankIdStatus() {
        switch (status.toLowerCase()) {
            case "complete":
                switch (Strings.nullToEmpty(hintCode).toLowerCase()) {
                    case "seb_unknown_bankid":
                        return BankIdStatus.FAILED_UNKNOWN;
                    default:
                        return BankIdStatus.DONE;
                }
            case "pending":
                return BankIdStatus.WAITING;
            case "failed":
                switch (Strings.nullToEmpty(hintCode).toLowerCase()) {
                    case "start_failed":
                        return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                    case "user_cancel":
                        return BankIdStatus.CANCELLED;
                    default:
                        return BankIdStatus.FAILED_UNKNOWN;
                }
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}

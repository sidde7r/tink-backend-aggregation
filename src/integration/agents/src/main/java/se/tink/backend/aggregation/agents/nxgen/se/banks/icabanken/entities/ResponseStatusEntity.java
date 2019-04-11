package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseStatusEntity {
    @JsonProperty("Code")
    private int code;

    @JsonProperty("ServerMessage")
    private String serverMessage;

    @JsonProperty("ClientMessage")
    private String clientMessage;

    public int getCode() {
        return code;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    @JsonIgnore
    public boolean isNotACustomer() {
        return serverMessage != null
                && serverMessage
                        .toLowerCase()
                        .contains(IcaBankenConstants.BankIdStatus.NOT_A_CUSTOMER);
    }

    @JsonIgnore
    public boolean isInterrupted() {
        return serverMessage != null
                && serverMessage
                        .toLowerCase()
                        .contains(IcaBankenConstants.BankIdStatus.INTERRUPTED);
    }

    @JsonIgnore
    public boolean isNotVerified() {
        return clientMessage != null
                && clientMessage
                        .toLowerCase()
                        .contains(IcaBankenConstants.BankIdStatus.NOT_VERIFIED);
    }

    @JsonIgnore
    public boolean isSomethingWentWrong() {
        return clientMessage != null
                && clientMessage
                        .toLowerCase()
                        .contains(IcaBankenConstants.BankIdStatus.SOMETHING_WENT_WRONG);
    }
}

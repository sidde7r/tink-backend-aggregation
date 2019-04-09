package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Optional;

public class ResponseStatus {
    @JsonProperty("ClientMessage")
    private String clientMessage;

    @JsonProperty("Code")
    private int code;

    @JsonProperty("ServerMessage")
    private String serverMessage;

    public String getClientMessage() {
        return Optional.ofNullable(clientMessage).orElse("");
    }

    public int getCode() {
        return code;
    }

    public String getServerMessage() {
        return Optional.ofNullable(serverMessage).orElse("");
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setServerMessage(String serverMessage) {
        this.serverMessage = serverMessage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("code", code)
                .add("clientMessage", clientMessage)
                .add("serverMessage", serverMessage)
                .toString();
    }
}

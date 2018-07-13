package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
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
}

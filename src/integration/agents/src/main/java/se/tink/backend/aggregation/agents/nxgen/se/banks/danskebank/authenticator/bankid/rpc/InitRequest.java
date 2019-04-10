package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class InitRequest {
    @JsonProperty("channel")
    private static final String CHANNEL = "O";

    @JsonProperty("package")
    private final String logonPackage;

    private InitRequest(String logonPackage) {
        this.logonPackage = logonPackage;
    }

    public static InitRequest createFromMessage(String logonPackage) {
        return new InitRequest(logonPackage);
    }

    public String getChannel() {
        return CHANNEL;
    }

    public String getLogonPackage() {
        return logonPackage;
    }
}

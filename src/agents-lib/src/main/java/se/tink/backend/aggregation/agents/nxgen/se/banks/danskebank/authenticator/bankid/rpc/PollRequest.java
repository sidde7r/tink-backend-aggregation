package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PollRequest {
    @JsonProperty("package")
    private String reference;
    @JsonProperty("channel")
    private static final String CHANNEL = "O";

    private PollRequest(String reference) {
        this.reference = reference;
    }

    public static PollRequest createFromReference(String reference) {
        return new PollRequest(reference);
    }

    public String getReference() {
        return reference;
    }

    public static String getCHANNEL() {
        return CHANNEL;
    }
}

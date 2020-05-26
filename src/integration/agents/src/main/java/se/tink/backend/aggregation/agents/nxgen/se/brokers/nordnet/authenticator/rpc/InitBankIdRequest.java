package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InitBankIdRequest {
    @JsonProperty("subject")
    private final String ssn;

    private final boolean useAnotherDevice = true;

    public InitBankIdRequest(String ssn) {
        this.ssn = ssn;
    }

    public String getSsn() {
        return ssn;
    }

    public boolean isUseAnotherDevice() {
        return useAnotherDevice;
    }
}

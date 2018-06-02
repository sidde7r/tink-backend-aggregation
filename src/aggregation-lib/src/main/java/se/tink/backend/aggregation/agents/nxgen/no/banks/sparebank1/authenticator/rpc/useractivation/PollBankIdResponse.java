package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PollBankIdResponse {
    private String pollStatus;

    public String getPollStatus() {
        return pollStatus;
    }

    public void setPollStatus(String pollStatus) {
        this.pollStatus = pollStatus;
    }
}

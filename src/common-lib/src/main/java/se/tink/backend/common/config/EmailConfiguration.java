package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailConfiguration {

    @JsonProperty
    private boolean shouldSendBlockedUserMail = true;

    @JsonProperty
    private String mandrillApiKey = "7UGjQFNOFLx4QpzG82j7GQ";

    public boolean shouldSendBlockedUserMail() {
        return shouldSendBlockedUserMail;
    }

    public String getMandrillApiKey() {
        return mandrillApiKey;
    }
}

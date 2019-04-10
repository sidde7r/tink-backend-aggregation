package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageEntity {
    @JsonProperty("errormsg")
    private String errorMessage;

    @JsonProperty("usermsg")
    private String userMessage;

    private String applicationData;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getApplicationData() {
        return applicationData;
    }
}

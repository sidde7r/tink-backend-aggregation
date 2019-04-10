package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusEntity {
    private boolean error;

    @JsonProperty("usererror")
    private boolean userError;

    public boolean isError() {
        return error;
    }

    public boolean isUserError() {
        return userError;
    }
}

package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StrongLoginResponse {

    @JsonProperty("strongLoginResponse")
    private StrongLoginResponseIn strongResponseIn;

    public StrongLoginResponseIn getStrongResponseIn() {
        return strongResponseIn;
    }

    public void setStrongResponseIn(StrongLoginResponseIn strongResponseIn) {
        this.strongResponseIn = strongResponseIn;
    }

    public String getErrorCode() {
        if (strongResponseIn == null || strongResponseIn.getErrorMessage() == null) {
            return null;
        }

        return (String) ((Map) strongResponseIn.getErrorMessage().get("errorCode")).get("$");
    }

    public boolean isError() {
        return getErrorCode() != null;
    }
}

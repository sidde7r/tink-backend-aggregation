package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AutoStartResponse {
    @JsonProperty("State")
    private int state;

    @JsonProperty("IsMobileDevice")
    private boolean isMobileDevice;

    @JsonProperty("AutoStartUrl")
    private String autoStartUrl = "";

    @JsonProperty("IsAjaxResponse")
    private boolean isAjaxResponse;

    @JsonIgnore
    public String getAutoStartUrl() {
        return autoStartUrl;
    }
}

package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RESULTO01 {
    @JsonProperty("INIT_TIME")
    private String initTime;

    @JsonProperty("INIT_RESULT")
    private String initResult;

    public String getInitTime() {
        return initTime;
    }

    public void setInitTime(String initTime) {
        this.initTime = initTime;
    }

    public String getInitResult() {
        return initResult;
    }

    public void setInitResult(String initResult) {
        this.initResult = initResult;
    }
}

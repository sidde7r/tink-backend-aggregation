package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessage {
    protected Map<String, Object> errorCode = new HashMap<String, Object>();

    public Map<String, Object> getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Map<String, Object> errorCode) {
        this.errorCode = errorCode;
    }
}

package se.tink.backend.aggregation.agents.banks.nordea.v20.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankingServiceResponse {
    private Map<String, Object> errorMessage = new HashMap<String, Object>();

    public Map<String, Object> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Map<String, Object> errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @SuppressWarnings("rawtypes")
    public Optional<String> getErrorCode() {
        if (getErrorMessage() != null) {
        
            Map<String, Object> errorMessage = getErrorMessage();

            if (errorMessage.containsKey("errorCode")) {
                
                Map errorCodes = (Map) errorMessage.get("errorCode"); 
                
                if (errorMessage instanceof Map) {
                    Object value = errorCodes.get("$");

                    return Optional.ofNullable((String) value);
                }
            }
        }
        
        return Optional.empty();
    }

    public boolean isError() {
        return getErrorCode().isPresent();
    }
}

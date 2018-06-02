package se.tink.backend.aggregation.agents.banks.handelsbanken.v6;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AbstractResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FailedResponse extends AbstractResponse {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

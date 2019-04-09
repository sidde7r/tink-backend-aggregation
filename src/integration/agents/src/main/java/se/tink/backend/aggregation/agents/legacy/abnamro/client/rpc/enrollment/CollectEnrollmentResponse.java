package se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectEnrollmentResponse {
    private String bcNumber;

    public String getBcNumber() {
        return bcNumber;
    }

    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    /** User has been enrolled if we get a bcNumber in the response. */
    public boolean isCompleted() {
        return !Strings.isNullOrEmpty(bcNumber);
    }
}

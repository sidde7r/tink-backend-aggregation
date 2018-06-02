package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestsResponse {

    @JsonProperty("rantor")
    private List<InterestRateEntity> interestRates;

    public List<InterestRateEntity> getInterestRates() {
        return interestRates;
    }

    public void setInterestRates(
            List<InterestRateEntity> interestRates) {
        this.interestRates = interestRates;
    }
}

package se.tink.backend.aggregation.agents.banks.sbab.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BorrowerWithOldFields extends Borrower {
    // If the borrower has been employed for at least twelve months (required).
    @JsonProperty("anstalldMinst12Manader")
    private Boolean hasBeenEmployedForAtLeastTwelveMonths;

    public Boolean isHasBeenEmployedForAtLeastTwelveMonths() {
        return hasBeenEmployedForAtLeastTwelveMonths;
    }

    public void setHasBeenEmployedForAtLeastTwelveMonths(Boolean hasBeenEmployedForAtLeastTwelveMonths) {
        this.hasBeenEmployedForAtLeastTwelveMonths = hasBeenEmployedForAtLeastTwelveMonths;
    }
}

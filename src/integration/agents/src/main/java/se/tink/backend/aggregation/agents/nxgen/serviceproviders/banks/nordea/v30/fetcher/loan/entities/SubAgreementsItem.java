package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubAgreementsItem {

    @JsonProperty("amount")
    private AmountEntity amount;

    @JsonProperty("interest")
    private InterestEntity interest;

    public AmountEntity getAmount() {
        return amount;
    }

    public InterestEntity getInterest() {
        return interest;
    }
}

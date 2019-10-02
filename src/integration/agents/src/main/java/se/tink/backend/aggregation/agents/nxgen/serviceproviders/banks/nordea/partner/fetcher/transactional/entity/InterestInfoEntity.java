package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestInfoEntity {
    @JsonProperty("interest_due_date")
    private String interestDueDate;

    private List<InterestsEntity> interests;
}

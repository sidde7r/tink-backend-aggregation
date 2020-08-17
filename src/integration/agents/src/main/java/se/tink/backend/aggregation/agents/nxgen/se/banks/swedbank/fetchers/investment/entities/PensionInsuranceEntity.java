package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionInsuranceEntity {
    private List<Object> holdings;

    @JsonIgnore
    public boolean hasNonEmptyHoldingsList() {
        return holdings != null && !holdings.isEmpty();
    }
}

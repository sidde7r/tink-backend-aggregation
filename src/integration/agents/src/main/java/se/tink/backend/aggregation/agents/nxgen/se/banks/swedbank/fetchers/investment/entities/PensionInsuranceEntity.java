package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc.AbstractInvestmentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionInsuranceEntity extends AbstractInvestmentAccountEntity {

    @JsonIgnore
    public boolean hasNonEmptyHoldingsList() {
        return holdings != null && !holdings.isEmpty();
    }
}

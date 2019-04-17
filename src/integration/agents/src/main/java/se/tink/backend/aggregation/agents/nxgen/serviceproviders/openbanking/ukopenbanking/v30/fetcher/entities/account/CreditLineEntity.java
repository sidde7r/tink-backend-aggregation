package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditLineEntity {
    @JsonProperty("Included")
    private boolean included;

    @JsonProperty("Amount")
    private AmountEntity amount;

    @JsonProperty("Type")
    private UkOpenBankingApiDefinitions.ExternalLimitType type;

    public boolean isIncluded() {
        return included;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public UkOpenBankingApiDefinitions.ExternalLimitType getType() {
        return type;
    }
}

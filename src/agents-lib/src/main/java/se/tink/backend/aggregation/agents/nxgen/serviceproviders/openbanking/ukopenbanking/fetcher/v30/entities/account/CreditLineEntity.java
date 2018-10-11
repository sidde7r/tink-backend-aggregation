package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditLineEntity {
    @JsonProperty("Included")
    private boolean included;
    @JsonProperty("Amount")
    private AmountEntity amount;
    @JsonProperty("Type")
    private UkOpenBankingConstants.ExternalLimitType type;

    public boolean isIncluded() {
        return included;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public UkOpenBankingConstants.ExternalLimitType getType() {
        return type;
    }
}

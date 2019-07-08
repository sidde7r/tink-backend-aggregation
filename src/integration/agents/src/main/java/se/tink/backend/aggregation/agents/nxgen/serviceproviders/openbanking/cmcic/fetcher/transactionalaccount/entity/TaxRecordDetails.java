package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TaxRecordDetails {
    @JsonProperty("period")
    private TaxPeriodEntity period = null;

    @JsonProperty("amount")
    private AmountTypeEntity amount = null;

    public TaxPeriodEntity getPeriod() {
        return period;
    }

    public void setPeriod(TaxPeriodEntity period) {
        this.period = period;
    }

    public AmountTypeEntity getAmount() {
        return amount;
    }

    public void setAmount(AmountTypeEntity amount) {
        this.amount = amount;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TaxRecordDetails {
    @JsonProperty("period")
    private TaxPeriodEntity period = null;

    @JsonProperty("amount")
    private AmountTypeEntity amount = null;
}

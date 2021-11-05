package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class TaxAmountEntity {
    @JsonProperty("rate")
    private Float rate = null;

    @JsonProperty("taxableBaseAmount")
    private AmountTypeEntity taxableBaseAmount = null;

    @JsonProperty("totalAmount")
    private AmountTypeEntity totalAmount = null;

    @JsonProperty("details")
    private List<TaxRecordDetails> details = null;
}

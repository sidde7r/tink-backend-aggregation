package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestIntervalsEntity {
    @JsonProperty("from_amount")
    private BigDecimal fromAmount;

    @JsonProperty("to_amount")
    private BigDecimal toAmount;

    private BigDecimal rate;
}

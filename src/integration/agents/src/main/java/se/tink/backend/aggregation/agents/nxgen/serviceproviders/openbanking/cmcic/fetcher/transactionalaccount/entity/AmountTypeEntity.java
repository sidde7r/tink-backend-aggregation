package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@Getter
public class AmountTypeEntity {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private String amount;

    @JsonCreator
    public AmountTypeEntity(
            @JsonProperty("currency") String currency, @JsonProperty("amount") String amount) {
        this.currency = currency;
        this.amount = amount;
    }
}

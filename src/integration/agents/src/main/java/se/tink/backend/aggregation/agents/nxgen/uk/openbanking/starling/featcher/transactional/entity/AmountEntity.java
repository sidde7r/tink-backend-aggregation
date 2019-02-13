package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity extends Amount {

    @JsonCreator
    public AmountEntity(
            @JsonProperty("currency") String currency, @JsonProperty("minorUnits") long value) {
        super(currency, value, 2);
    }
}

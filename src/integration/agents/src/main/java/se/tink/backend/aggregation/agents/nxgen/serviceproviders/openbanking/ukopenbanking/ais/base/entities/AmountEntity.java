package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AmountEntity {

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Amount")
    private String unsignedAmount;
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentNameBody {
    @JsonProperty("Name")
    private String name;

    public String getName() {
        Preconditions.checkState(name != null, "PaymentBody is null");
        return name;
    }
}

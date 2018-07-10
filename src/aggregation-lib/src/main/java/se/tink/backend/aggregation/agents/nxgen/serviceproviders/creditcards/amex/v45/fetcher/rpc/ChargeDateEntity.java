package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChargeDateEntity {
    private String formattedDate;
    private int rawValue;

    public String getFormattedDate() {
        return formattedDate;
    }

    public int getRawValue() {
        return rawValue;
    }
}

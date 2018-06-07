package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChargeDateEntity {
    private String formattedDate;
    private long rawValue;

    public String getFormattedDate() {
        return formattedDate;
    }

    public long getRawValue() {
        return rawValue;
    }

    public Date toDate(){
        return new Date(rawValue);
    }
}

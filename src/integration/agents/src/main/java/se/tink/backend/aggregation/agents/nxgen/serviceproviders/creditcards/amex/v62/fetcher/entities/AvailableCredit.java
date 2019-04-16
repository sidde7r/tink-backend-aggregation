package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AvailableCredit {

    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}

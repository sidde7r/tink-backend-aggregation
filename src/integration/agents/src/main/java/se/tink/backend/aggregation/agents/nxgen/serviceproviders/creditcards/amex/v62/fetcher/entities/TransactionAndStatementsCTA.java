package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAndStatementsCTA {

    private String iconName;
    private String label;

    public String getIconName() {
        return iconName;
    }

    public String getLabel() {
        return label;
    }
}

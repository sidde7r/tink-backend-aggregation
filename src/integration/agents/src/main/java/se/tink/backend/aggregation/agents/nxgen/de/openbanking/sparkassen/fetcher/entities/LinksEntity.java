package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href balances;

    public Href getBalances() {
        return balances;
    }
}

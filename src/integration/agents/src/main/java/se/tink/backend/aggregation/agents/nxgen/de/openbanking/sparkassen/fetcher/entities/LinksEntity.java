package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href balances;
    private Href transactions;

    public Href getBalances() {
        return balances;
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href balances;
    private Href transactions;
}

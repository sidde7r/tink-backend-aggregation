package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MarketsEntity {
    private String position;
    private String name;
    private AmountEntity amount;
    private String code;

    public String getName() {
        return name;
    }
}

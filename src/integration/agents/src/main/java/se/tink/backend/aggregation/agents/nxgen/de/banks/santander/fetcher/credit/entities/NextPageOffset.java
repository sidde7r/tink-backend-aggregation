package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NextPageOffset {

    @JsonProperty("numMovimientoRepo")
    private int numMovimientoRepo;

    @JsonProperty("idPASRepo")
    private String idPASRepo;

    @JsonProperty("concepEconRepo")
    private ConcepEconRepo concepEconRepo;
}

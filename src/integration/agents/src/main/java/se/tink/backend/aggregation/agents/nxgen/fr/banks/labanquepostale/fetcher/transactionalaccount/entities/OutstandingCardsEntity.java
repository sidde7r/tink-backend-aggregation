package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OutstandingCardsEntity {
    @JsonProperty("cumul")
    private double plurality;

    private Object dateImputation;
}

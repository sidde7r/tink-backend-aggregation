package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardDetailsBodyEntity {

    @JsonProperty("DataHoje")
    private String dateToday;

    //    @JsonProperty("Movimentos")
    //    private CardMovemenetEntity movements;

    // `Movimentos` is null - cannot define it!
    @JsonProperty("ContextoCartoes")
    private ContextCardsEntity contextCards;

    @JsonProperty("ComSCA")
    private boolean withSCA;

    public ContextCardsEntity getContextCards() {
        return contextCards;
    }
}

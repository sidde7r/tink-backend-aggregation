package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.CardsListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.GenericCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericCardsResponse {

    @JsonProperty("listaTarjetasGenerica")
    private CardsListEntity cards;

    @JsonProperty("numTarjsRetornadas")
    private String onAReturnedTargets;

    @JsonProperty("claveContinuacionTcrindco")
    private String keyContinuationTcrindco;

    @JsonProperty("claveContinuacionNumtar")
    private String keyContinuationNumtar;

    public Collection<GenericCardEntity> getCards() {
        return cards.getCards();
    }
}

package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.CardsListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.GenericCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

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

    public Collection<CreditCardAccount> toTinkCards() {
        return cards.getCards().stream()
                .filter(GenericCardEntity::isCreditCard)
                .map(GenericCardEntity::toTinkCard)
                .collect(Collectors.toList());
    }

}

package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.CardsListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.GenericCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

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
        if (cards == null) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(cards.getCards()).orElse(Collections.emptyList()).stream()
                .filter(GenericCardEntity::isCreditCard)
                .map(GenericCardEntity::toTinkCard)
                .collect(Collectors.toList());
    }
}

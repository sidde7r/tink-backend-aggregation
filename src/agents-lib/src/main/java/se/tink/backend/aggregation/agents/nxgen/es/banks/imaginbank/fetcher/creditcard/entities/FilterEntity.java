package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterEntity {

    @JsonProperty("listaTarjetas")
    private List<String> listCards;
    @JsonProperty("listaFraccionables")
    private List<String> fractionalList;

    public FilterEntity(String cardId) {
        listCards = Collections.singletonList(cardId);
        fractionalList = Collections.singletonList(ImaginBankConstants.CreditCard.FRACTIONAL_LIST_FILTER);
    }
}

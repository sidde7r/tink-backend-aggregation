package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

@JsonObject
public class CreditCardResponse {

    private List<CreditCardEntity> cards;
    private boolean moreData;

    @JsonProperty("listaTarjetasGenerica")
    private void setCards(JsonNode node) throws IOException {

        moreData = node.get("masDatos").asBoolean();

        // Skip level in JSON tree
        node = node.get("tarjetaGenerica");

        cards= new ObjectMapper().readValue(node.traverse(),
                new TypeReference<List<CreditCardEntity>>(){});
    }

    public Collection<CreditCardAccount> toTinkCards(){

        return cards.stream()
                .map(CreditCardEntity::toTinkCard)
                .collect(Collectors.toList());
    }
}

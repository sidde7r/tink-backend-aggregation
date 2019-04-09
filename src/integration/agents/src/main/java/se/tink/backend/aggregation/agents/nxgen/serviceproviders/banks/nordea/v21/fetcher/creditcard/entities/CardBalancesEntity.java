package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBalancesEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("cards")
    private List<CardBalanceEntity> cardBalances;

    public List<CardBalanceEntity> getCardBalances() {
        return cardBalances != null ? cardBalances : Collections.emptyList();
    }

    public void setCardBalances(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Map) {
            cardBalances =
                    Collections.singletonList(MAPPER.convertValue(obj, CardBalanceEntity.class));
        } else {
            cardBalances =
                    MAPPER.convertValue(obj, new TypeReference<List<CardBalanceEntity>>() {});
        }
    }
}

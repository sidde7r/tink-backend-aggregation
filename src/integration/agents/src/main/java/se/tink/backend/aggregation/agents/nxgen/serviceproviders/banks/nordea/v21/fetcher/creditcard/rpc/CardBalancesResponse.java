package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CardBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CardBalancesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBalancesResponse extends NordeaResponse {
    @JsonProperty("getCardBalancesOut")
    private CardBalancesEntity cardBalancesEntity;

    @JsonIgnore
    public List<CardBalanceEntity> getCardBalances() {
        return cardBalancesEntity.getCardBalances();
    }
}

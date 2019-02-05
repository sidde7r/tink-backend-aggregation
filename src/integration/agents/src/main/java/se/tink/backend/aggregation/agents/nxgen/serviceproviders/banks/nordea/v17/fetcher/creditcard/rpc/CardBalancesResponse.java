package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.GetCardBalancesOutEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBalancesResponse extends NordeaResponse {
    private GetCardBalancesOutEntity getCardBalancesOut;

    public GetCardBalancesOutEntity getGetCardBalancesOut() {
        return getCardBalancesOut;
    }
}

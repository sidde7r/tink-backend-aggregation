package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DepositsResponse {
    private AmountEntity amount;
    private List<Object> accountsPositions;

    public AmountEntity getAmount() {
        return amount;
    }

    public List<Object> getAccountsPositions() {
        return accountsPositions;
    }
}

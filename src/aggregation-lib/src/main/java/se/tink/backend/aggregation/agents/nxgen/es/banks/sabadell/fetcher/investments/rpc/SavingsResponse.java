package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsResponse {
    private AmountEntity total;
    private List<Object> savingPlans;

    public AmountEntity getTotal() {
        return total;
    }

    public List<Object> getSavingPlans() {
        return savingPlans;
    }
}

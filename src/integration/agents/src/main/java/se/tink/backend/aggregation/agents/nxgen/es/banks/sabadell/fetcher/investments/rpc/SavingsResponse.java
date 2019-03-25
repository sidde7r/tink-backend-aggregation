package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities.SavingPlansEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class SavingsResponse {
    private AmountEntity total;
    private List<SavingPlansEntity> savingPlans;

    public AmountEntity getTotal() {
        return total;
    }

    public List<SavingPlansEntity> getSavingPlans() {
        return savingPlans;
    }
}

package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.collection.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecuritiesPortfolioEntity extends AbstractContractDetailsEntity {

    private List<SecurityEntity> securities;
    private AmountEntity balance;

    public List<SecurityEntity> getSecurities() {
        return securities;
    }

    public AmountEntity getBalance() {
        return balance;
    }
}

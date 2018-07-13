package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DivisaEntity {
    private AmountEntity amount;
    private List<AccountEntity> accounts;

    public AmountEntity getAmount() {
        return amount;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
